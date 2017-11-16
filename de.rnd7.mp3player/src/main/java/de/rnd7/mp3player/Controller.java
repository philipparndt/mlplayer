package de.rnd7.mp3player;

import static de.rnd7.mp3player.hw.HW.$;

import java.io.File;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import de.rnd7.mp3player.hw.BacklightTimerThread;
import de.rnd7.mp3player.library.FolderLibrary;
import de.rnd7.mp3player.library.LibraryItem;
import de.rnd7.mp3player.mplayer.MPlayer;
import de.rnd7.mp3player.mplayer.VolumeControl;

public class Controller {
	private final ScheduledExecutorService statuesRequestExecutor = Executors.newScheduledThreadPool(2);

	private final MPlayer player;
	private final VolumeControl volumeControl;
	private final Viewer viewer;

	private final FolderLibrary library;
	private final LongPressCommandExecutor longPressExecutor = new LongPressCommandExecutor();
	private final BacklightTimerThread backlight = BacklightTimerThread.createStarted();

	private final ModeManager modes = new ModeManager(this::isPlayingMode);

	public Controller(final Viewer viewer, final FolderLibrary library, final MPlayer player, final VolumeControl volumeControl) {
		this.viewer = viewer;
		this.library = library;
		this.player = player;
		this.volumeControl = volumeControl;

		this.statuesRequestExecutor.scheduleAtFixedRate(this::refresh, 0, 500, TimeUnit.MILLISECONDS);
		this.statuesRequestExecutor.scheduleAtFixedRate(this::saveProgress, 0, 5, TimeUnit.MINUTES);

		this.registerButtons();

		this.modes.addStopped(new Mode("navigate")
				.setCommandL(Images.load("left.png"), this::prevoius)
				.setCommandR(Images.load("right.png"), this::next));

		this.modes.addPlaying(new ModeShowTime()
				.setCommandL(Images.load("backward.png"), this::backward)
				.setCommandR(Images.load("forward.png"), this::forward))
		.addPlaying(new Mode("seek")
				.setCommandL(Images.load("backward.png"), this::backward)
				.setCommandR(Images.load("forward.png"), this::forward))
		.addPlaying(new ModeVolume(this.volumeControl)
				.setCommandL(Images.load("volume-down.png"), this.volumeControl::down)
				.setCommandR(Images.load("volume-up.png"), this.volumeControl::up));

		this.modes.initStoppedMode();
	}


	private void registerButtons() {
		$().buttons.button1.onPressed(this::button1);
		$().buttons.button1.onReleased(this.longPressExecutor::cancel);

		$().buttons.button2.onPressed(this::button2);
		$().buttons.button2.onReleased(this.longPressExecutor::cancel);

		$().buttons.button3.onPressed(this::button3);
		$().buttons.button3.onReleased(this.longPressExecutor::cancel);

		$().buttons.button4.onPressed(this::button4);
		$().buttons.button4.onReleased(this.longPressExecutor::cancel);
	}

	private void button1() {
		if (!this.backlight.reset()) {
			return;
		}

		final Optional<File> currentItem = this.library.getCurrentItem().map(LibraryItem::getFile);

		if (this.player.isPlaying(currentItem.orElse(null))) {
			if (this.player.isPaused()) {
				this.modes.initPlayingMode();
			}
			else {
				this.saveProgressNow();
				this.modes.initStoppedMode();
			}

			this.player.playPause();
			this.refresh();
		}
		else {
			this.play();
		}
	}

	private void button2() {
		if (!this.backlight.reset()) {
			return;
		}

		this.modes.toggleMode();
		this.refresh();
	}

	private void button3() {
		if (!this.backlight.reset()) {
			return;
		}

		if (!this.isPlayingMode()) {
			this.player.stop();
		}

		this.modes.getCurrentMode().getCommandL().ifPresent(this.longPressExecutor::schedule);

		this.refresh();
	}

	private void button4() {
		if (!this.backlight.reset()) {
			return;
		}

		if (!this.isPlayingMode()) {
			this.player.stop();
		}

		this.modes.getCurrentMode().getCommandR().ifPresent(this.longPressExecutor::schedule);

		this.refresh();
	}

	private void play() {
		final Optional<LibraryItem> currentItem = this.library.getCurrentItem();
		if (currentItem.isPresent()) {
			final LibraryItem item = currentItem.get();

			this.modes.initPlayingMode();

			this.showLoading();

			final int currentPosition = item.getCurrentPosition();
			if (currentPosition > 0) {
				this.player.play(item.getFile(), Duration.ofSeconds(currentPosition));
			}
			else {
				this.player.play(item.getFile());
			}
		}
	}

	private boolean isPlayingMode() {
		return this.player.isPlaying() && !this.player.isPaused();
	}

	private void next() {
		this.library.next();

		this.refresh();
	}

	private void prevoius() {
		this.library.previous();

		this.refresh();
	}

	private void forward() {
		this.player.forward(Duration.ofSeconds(10));
	}

	private void backward() {
		this.player.backward(Duration.ofSeconds(10));
	}

	private void saveProgress() {
		if (this.isPlayingMode()) {
			this.saveProgressNow();
		}
	}


	private void saveProgressNow() {
		this.library.getCurrentItem().ifPresent(item -> {
			final int length = this.player.getLength();
			final int position = this.player.getPosition();

			item.setPosition(position, length);
		});
	}

	private void refresh() {
		if (this.player.isLoading()) {
			return;
		}

		final boolean playing = this.player.isPlaying();
		this.viewer.setPaying(playing);
		
		final Optional<LibraryItem> currentItem = this.library.getCurrentItem();
		this.viewer.setCurrent(currentItem.flatMap(LibraryItem::getCover));

		if (playing) {
			this.viewer.setPaused(this.player.isPaused());
			this.viewer.setPosition(this.player.getPosition(), this.player.getLength());
		}
		else if (currentItem.isPresent()) {
			final LibraryItem item = currentItem.get();
			this.viewer.setPosition(item.getCurrentPosition(), item.getLength());
		}
		else {
			this.viewer.setPosition(0, 0);
		}

		this.viewer.setMode(this.modes.getCurrentMode());
		this.viewer.setModeCount(this.modes.activeModeList().size());
		this.viewer.update();
	}

	private void showLoading() {
		this.viewer.setCurrent(this.library.getCurrentItem().flatMap(LibraryItem::getCover));
		this.viewer.setPosition(0, Integer.MAX_VALUE);
		this.viewer.update();
	}
}
