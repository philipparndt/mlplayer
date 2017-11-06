package de.rnd7.mp3player;

import static de.rnd7.mp3player.hw.HW.$;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.time.Duration;

public class ModeShowTime extends Mode {
	public ModeShowTime() {
		super("time");
	}

	@Override
	public void paint(final Graphics2D graphics, final int length, final int position) {
		if (length < 0 || position < 0 || length == Integer.MAX_VALUE) {
			return;
		}

		final Duration l = Duration.ofSeconds(length);
		final Duration p = Duration.ofSeconds(position);

		final Duration remaining = l.minus(p);

		graphics.setColor(Color.WHITE);
		graphics.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 24));
		final String duration = "-" + this.format(remaining);

		final int width = graphics.getFontMetrics().stringWidth(duration);
		final int x = $().display.width - 10 - width;

		graphics.drawString(duration, x, 221);
	}

	private String format(final Duration d) {
		final long hours = d.toHours();
		final long minutes = d.minusHours(hours).toMinutes();
		final long secounds = d.minusHours(hours).minusMinutes(minutes).toMillis() / 1000;

		if (hours > 0) {
			return String.format("%d:%02d:%02d", hours, minutes, secounds);
		}
		else {
			return String.format("%02d:%02d", minutes, secounds);
		}
	}

}
