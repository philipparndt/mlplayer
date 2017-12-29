package de.rnd7.mp3player.mplayer;

import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;
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
	private LocalDateTime previousChapterTriggered = LocalDateTime.now();
	
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

		final MPlayerThread playerThread = new MPlayerThread(this.command);
		playerThread.start();

		final TimeLimiter timeLimiter = SimpleTimeLimiter.create(this.executor);
		try {
			timeLimiter.callWithTimeout(() -> this.waitForPlayer(playerThread), 5, TimeUnit.SECONDS);
			this.thread = Optional.of(playerThread);
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
		LOGGER.trace("PLAY: {}", file);

		this.playingFile = file;
		this.restart();
	}

	public void play(final File file) {
		this.loading = true;

		this.initPlay(file);

		this.thread.ifPresent(t -> t.loadAndStart(file));
		this.loading = false;
	}

	public boolean isLoading() {
		return this.loading;
	}

	public void play(final File file, final Duration position) {
		this.initPlay(file);

		this.thread.ifPresent(t -> {
			t.loadAndStart(file);
			t.seekToPosition(position);
		});
	}

	public boolean isPaused() {
		return this.thread.map(MPlayerThread::isPaused).orElse(false);
	}

	/**
	 * 
	 * @return the position of the current file in seconds
	 */
	public Duration getPosition() {
		return Duration.ofSeconds(this.thread.map(MPlayerThread::getPosition).orElse(-1));
	}

	/**
	 * 
	 * @return the length of the current file in seconds
	 */
	public Duration getLength() {
		return Duration.ofSeconds(this.thread.map(MPlayerThread::getLength).orElse(-1));
	}

	public void forward(final Duration duration) {
		this.thread.ifPresent(t -> t.forward(duration));
	}

	public void backward(final Duration duration) {
		this.thread.ifPresent(t -> t.backward(duration));
	}

	public void seek(final Duration position) {
		this.thread.ifPresent(t -> t.seekToPosition(position));
	}

	public int getChapter() {
		return this.thread.map(MPlayerThread::getChapter).orElse(-1);
	}
	
	public void setChapter(int chapter) {
		this.thread.ifPresent(player -> player.setChapter(chapter));
	}
	
	public int getChapters() {
		return this.thread.map(MPlayerThread::getChapters).orElse(-1);
	}

	public void nextChapter() {
		int chapters = getChapters();
		int chapter = getChapter();
		if (chapters >= 0) {
			if (chapter >=0 && (chapter + 1 < chapters)) {
				setChapter(chapter + 1);
			}
			else {
				// Seek end
				seek(getLength());
			}
		}
	}

	public void prevoiusChapter() {
		int chapters = getChapters();
		int chapter = Math.max(0, getChapter() - prevoiusChapterOffset());
		if (chapters >= 0) {
			setChapter(chapter);
		}
		
		previousChapterTriggered = LocalDateTime.now();
	}

	private int prevoiusChapterOffset() {
		return isTriggerCurrentChapter() ? 0 : 1;
	}

	private boolean isTriggerCurrentChapter() {
		boolean triggerCurrent = previousChapterTriggered.plus(Duration.ofSeconds(2)).isBefore(LocalDateTime.now());
		return triggerCurrent;
	}
}
