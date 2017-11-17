package de.rnd7.mp3player.mplayer;

import java.io.File;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.SimpleTimeLimiter;
import com.google.common.util.concurrent.TimeLimiter;

public class MPlayer {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(MPlayer.class);

	private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
	private Optional<MPlayerThread> thread = Optional.empty();
	private File playingFile;
	private final String command;
	private boolean loading;

	public MPlayer(final String command) {
		this.command = command;
	}

	public void restart() {

		this.thread.ifPresent(old -> {
			old.stopPlay();
			old.destroyPlayer();
		});

		final MPlayerThread thread = new MPlayerThread(this.command);
		thread.start();

		final TimeLimiter timeLimiter = SimpleTimeLimiter.create(this.executor);
		try {
			timeLimiter.callWithTimeout(() -> this.waitForPlayer(thread), 5, TimeUnit.SECONDS);
			this.thread = Optional.of(thread);
		} catch (final InterruptedException e) {
			LOGGER.info(e.getMessage(), e);
			Thread.currentThread().interrupt();
		} catch (TimeoutException | ExecutionException e) {
			LOGGER.error(e.getMessage(), e);
		}
	}

	private Void waitForPlayer(final MPlayerThread thread) {
		while (!thread.isReady()) {
			try {
				Thread.sleep(100);
			} catch (final InterruptedException e) {
				LOGGER.info(e.getMessage(), e);
				Thread.currentThread().interrupt();
			}
		}
		return null;
	}

	public boolean isPlaying() {
		return this.thread.map(MPlayerThread::isPlaying).orElse(false);
	}

	public boolean isPlaying(final File file) {
		return Objects.equals(this.playingFile, file) && this.isPlaying();
	}

	public void playPause() {
		this.thread.ifPresent(MPlayerThread::playPause);
	}

	public void stop() {
		this.thread.ifPresent(old -> {
			old.stopPlay();
			old.destroyPlayer();
		});

		this.thread = Optional.empty();
	}

	private void initPlay(final File file) {
		LOGGER.trace("PLAY: " + file);

		this.playingFile = file;
		this.restart();
	}

	public void play(final File file) {
		this.loading = true;

		this.initPlay(file);

		this.thread.ifPresent(thread -> thread.loadAndStart(file));
		this.loading = false;
	}

	public boolean isLoading() {
		return this.loading;
	}

	public void play(final File file, final Duration position) {
		this.initPlay(file);

		this.thread.ifPresent(thread -> {
			thread.loadAndStart(file);
			thread.seek(position);
		});
	}

	public boolean isPaused() {
		return this.thread.map(MPlayerThread::isPaused).orElse(false);
	}

	public void decreaseVolume() {
		this.thread.ifPresent(MPlayerThread::decreaseVolume);
	}

	public void increaseVolume() {
		this.thread.ifPresent(MPlayerThread::increaseVolume);
	}

	public int getPosition() {
		return this.thread.map(MPlayerThread::getPosition).orElse(-1);
	}

	public int getLength() {
		return this.thread.map(MPlayerThread::getLength).orElse(-1);
	}

	public void forward(final Duration duration) {
		this.thread.ifPresent(thread -> thread.forward(duration));
	}

	public void backward(final Duration duration) {
		this.thread.ifPresent(thread -> thread.backward(duration));
	}

	public void seek(final Duration position) {
		this.thread.ifPresent(thread -> thread.seek(position));
	}

}
