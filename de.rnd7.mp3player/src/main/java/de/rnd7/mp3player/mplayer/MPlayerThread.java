package de.rnd7.mp3player.mplayer;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.SimpleTimeLimiter;
import com.google.common.util.concurrent.TimeLimiter;

@SuppressWarnings("squid:S3457")
class MPlayerThread extends Thread {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(MPlayerThread.class);

	private static final ExecutorService SERVICE = Executors.newFixedThreadPool(1) ;

	private InputStreamReader input;
	private PrintStream output;
	private Process process;
	private boolean ready = false;

	private final ReentrantLock lock = new ReentrantLock();

	private final String command;

	public MPlayerThread(final String command) {
		this.command = command;
	}

	@Override
	public void run() {
		try {
			LOGGER.trace("Executing command: {}", this.command);

			final ProcessBuilder pb = new ProcessBuilder("bash", "-c", this.command);
			this.process = pb.start();

			this.input = new InputStreamReader(this.process.getInputStream());
			this.output = new PrintStream(this.process.getOutputStream());

			this.ready = true;

			this.process.waitFor();

			LOGGER.trace("MPlayer instance stopped.");

		} catch (final Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
	}

	public boolean isReady() {
		return this.ready;
	}

	public void destroyPlayer() {
		this.process.destroy();
	}

	private void sendPropertyRequestInternal(final String propertyName) {
		this.sendRequest(String.format("get_property %s\n",propertyName));
	}

	void sendRequest(final String request) {
		LOGGER.trace("request: {}", request);
		this.output.print(request);
		this.output.flush();
	}

	protected List<String> readLinesUntil(final Predicate<String> stopCriteria) {
		final BufferedReader bufferedReader = IOUtils.toBufferedReader(this.input);

		final TimeLimiter timeLimiter = SimpleTimeLimiter.create(SERVICE);

		final Callable<List<String>> function = () -> this.readLines(stopCriteria, bufferedReader);
		try {
			return timeLimiter.callWithTimeout(function, 2, TimeUnit.SECONDS);
		} catch (TimeoutException | InterruptedException | ExecutionException e) {
			LOGGER.error(e.getMessage(), e);
			return Collections.emptyList();
		}
	}

	private List<String> readLines(final Predicate<String> stopCriteria, final BufferedReader bufferedReader) throws IOException {
		final List<String> lines = new ArrayList<>();
		String line;
		while ((line = bufferedReader.readLine()) != null) {
			LOGGER.trace("responce: '{}'", line);

			lines.add(line.trim());
			if (stopCriteria.test(line.trim()) || line.trim().startsWith("ANS_ERROR")) {
				break;
			}
		}
		return lines;
	}

	public List<String> sendRequest(final String request, final Predicate<String> stopCriteria) {
		this.lock.lock();
		try {
			this.sendRequest(String.format(request));
			return this.readLinesUntil(stopCriteria);
		}
		finally {
			this.lock.unlock();
		}
	}

	private static class PropertyResultCriteria implements Predicate<String> {
		private final String propertyName;

		public PropertyResultCriteria(final String propertyName) {
			this.propertyName = propertyName;
		}

		@Override
		public boolean test(final String response) {
			return response.startsWith("ANS_" + this.propertyName +"=");
		}

		@Override
		public String toString() {
			return this.propertyName;
		}
	}

	public List<String> sendPropertyRequest(final String propertyName) {
		this.lock.lock();
		try {
			this.sendPropertyRequestInternal(propertyName);
			return this.readLinesUntil(new PropertyResultCriteria(propertyName));
		}
		finally {
			this.lock.unlock();
		}
	}

	public void loadAndStart(final File file) {
		LOGGER.trace("sending load: '{}'", file);
		this.sendRequest(String.format("load \"%s\"\n", file.getAbsolutePath()), l -> l.contains("Starting playback"));
	}

	public void playPause() {
		sendRequest("pause\n");
	}

	public void stopPlay() {
		sendRequest("stop\n");
	}

	public int getLength() {
		return new PropertyIntegerCommand("length", this).read();
	}

	public int getPosition() {
		return new PropertyIntegerCommand("time_pos", this).read();
	}

	public boolean isPlaying() {
		return new PropertyIntegerCommand("time_pos", this).read() >= 0;
	}

	public int getChapters() {
		return new PropertyIntegerCommand("chapters", this).read();
	}
	
	public int getChapter() {
		return new PropertyIntegerCommand("chapter", this).read();
	}
	
	public void setChapter(int chapter) {
		 new PropertyIntegerCommand("chapter", this).write(chapter);
	}
	
	public boolean isPaused() {
		return new ReadBooleanCommand("pause", this).read();
	}

	public void forward(final Duration duration) {
		seek(duration.getSeconds(), SeekType.RELATIVE);
	}

	public void backward(final Duration duration) {
		seek(Duration.ZERO.minus(duration).getSeconds(), SeekType.RELATIVE);
	}

	public void seekToPosition(final Duration position) {
		seek(position.getSeconds(), SeekType.ABSOLUTE);
	}
	
	public void seekToPercentage(final int percentage) {
		seek(percentage, SeekType.PERCENTAGE);
	}
	
	public void seek(long value, SeekType type) {
		sendRequest(String.format("seek %d %d\n", value, type.ordinal()));
	}
}
