package de.rnd7.mp3player;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

class ModeManager {

	private final List<Mode> stoppedModes = new ArrayList<>();
	private final List<Mode> playingModes = new ArrayList<>();

	private Mode currentMode;

	private Mode stoppedMode;
	private Mode playingMode;
	private final Supplier<Boolean> isPlaying;

	public ModeManager(final Supplier<Boolean> isPlaying) {
		this.isPlaying = isPlaying;
	}

	void initPlayingMode() {
		this.currentMode = this.playingMode != null ? this.playingMode : this.playingModes.get(0);
	}

	void initStoppedMode() {
		this.currentMode = this.stoppedMode != null ? this.stoppedMode : this.stoppedModes.get(0);
	}

	public Mode getCurrentMode() {
		return this.currentMode;
	}

	void toggleMode() {
		final List<Mode> modes = this.activeModeList();

		int current = modes.indexOf(this.currentMode);
		if (current < 0) {
			this.currentMode = modes.get(0);
		}
		else {
			current++;
			if (current > modes.size() - 1) {
				current = 0;
			}

			this.currentMode = modes.get(current);
		}

		if (this.isPlayingMode()) {
			this.playingMode = this.currentMode;
		}
		else {
			this.stoppedMode = this.currentMode;
		}
	}

	List<Mode> activeModeList() {
		final List<Mode> modes;
		if (this.isPlayingMode()) {
			modes = this.playingModes;
		}
		else {
			modes = this.stoppedModes;
		}
		return modes;
	}

	private boolean isPlayingMode() {
		return this.isPlaying.get();
	}

	public ModeManager addStopped(final Mode mode) {
		this.stoppedModes.add(mode);

		return this;
	}

	public ModeManager addPlaying(final Mode mode) {
		this.playingModes.add(mode);

		return this;
	}

	public List<Mode> getPlayingModes() {
		return this.playingModes;
	}

}
