package de.rnd7.mp3player.mplayer;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VolumeControl {
	private static final Logger LOGGER = LoggerFactory.getLogger(VolumeControl.class);

	private static final int DELTA = 2;

	private int volume;
	private final String pattern;
	private final int min;
	private final int max;

	public VolumeControl(final String pattern, final int min, final int max) {
		this.pattern = pattern;
		this.min = min;
		this.max = max;
		this.volume = min;
	}

	public VolumeControl set(final int percentage) {
		this.volume = Math.max(this.min, Math.min(this.max, percentage));

		this.apply();

		return this;
	}

	private void apply() {
		final String command = String.format(this.pattern, this.volume);
		try {
			Runtime.getRuntime().exec(command);
		} catch (final IOException e) {
			LOGGER.error(e.getMessage(), e);
		}
	}

	public int getVolume() {
		return this.volume;
	}

	public int getMin() {
		return this.min;
	}

	public int getMax() {
		return this.max;
	}

	public VolumeControl up() {
		this.volume = Math.min(this.max, this.volume + DELTA);

		this.apply();

		return this;
	}

	public VolumeControl down() {
		this.volume = Math.max(this.min, this.volume - DELTA);

		this.apply();

		return this;
	}
}
