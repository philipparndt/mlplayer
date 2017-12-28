package de.rnd7.mp3player;

import static de.rnd7.mp3player.hw.HW.$;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.time.Duration;

public class ModeShowChapter extends Mode {
	public ModeShowChapter() {
		super("time");
	}

	@Override
	public void paint(final Graphics2D graphics, final Duration length, final Duration position, int chapters, int chapter) {
		if (!isPlaying(length, position)) {
			return;
		}

		graphics.setColor(Color.WHITE);
		graphics.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 24));
		final String duration = String.format("%d / %d", chapter + 1, chapters);

		final int width = graphics.getFontMetrics().stringWidth(duration);
		final int x = $().display.getWidth() - 10 - width;

		graphics.drawString(duration, x, 221);
	}

}
