package de.rnd7.mp3player;

import static de.rnd7.mp3player.hw.HW.$;

import java.awt.Color;
import java.awt.Graphics2D;

import de.rnd7.mp3player.mplayer.VolumeControl;

public class ModeVolume extends Mode {
	private final VolumeControl volume;

	public ModeVolume(final VolumeControl volume) {
		super("volumne");
		this.volume = volume;
	}

	@Override
	public void paint(final Graphics2D graphics, final int length, final int position) {
		this.drawProgress(graphics);

		super.paint(graphics, length, position);
	}

	private void drawProgress(final Graphics2D graphics) {
		final int totalWidth = $().display.width - $().display.b3;

		final int min = this.volume.getMin();
		final int max = this.volume.getMax();

		final int total = max - min;
		final int vol = this.volume.getVolume();
		final int current = vol - min;

		final Double len = (double) totalWidth / (double) total * current;

		if (vol >= 90) {
			graphics.setColor(new Color(255, 60, 0, 100));
		}
		else {
			graphics.setColor(new Color(255, 255, 255, 100));
		}
		graphics.fillRect($().display.width - totalWidth - 5, 195, len.intValue(), 40);
	}

}
