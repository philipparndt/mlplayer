package de.rnd7.mp3player;

import static de.rnd7.mp3player.hw.HW.$;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.Duration;
import java.util.Optional;

public class Viewer {
	private final BufferedImage play;
	private final BufferedImage pause;
	private final BufferedImage bottomBar;

	private final BufferedImage mode;

	private boolean playing = false;
	private boolean paused = false;
	private Duration position = Duration.ZERO;
	private Duration length = Duration.ZERO;
	private Optional<Image> cover = Optional.empty();
	private Mode currentMode;
	private int modeCount;
	private int chapters;
	private int chapter;
	
	public Viewer() throws IOException {
		this.play = Images.load("play.png");
		this.pause = Images.load("pause.png");
		this.bottomBar = Images.load("bottom-bar.png");

		this.mode = Images.load("mode.png");

		this.update();
	}

	public void setPaying(final boolean playing) {
		this.playing = playing;
	}

	public void setMode(final Mode mode) {
		this.currentMode = mode;
	}

	public void setModeCount(final int modeCount) {
		this.modeCount = modeCount;
	}

	public void setPaused(final boolean paused) {
		this.paused = paused;
	}

	public void setPosition(final Duration position, final Duration length) {
		this.position = position;
		this.length = length;
	}

	public void setCurrent(final Optional<Image> cover) {
		this.cover = cover;
	}

	public void setChapter(int chapters, int chapter) {
		this.chapters = chapters;
		this.chapter = chapter;
	}
	
	public void update() {
		final Graphics2D graphics = $().display.graphics;

		graphics.setColor(Color.BLACK);
		graphics.fillRect(0, 0, 320, 240);

		this.cover.ifPresent(c -> graphics.drawImage(c, 0, 0, null));

		graphics.drawImage(this.bottomBar, 0, 185, null);


		if (this.length.getSeconds() == Long.MAX_VALUE) {
			graphics.setColor(Color.WHITE);
			graphics.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 24));
			graphics.drawString("Loading...", $().display.getB1(), $().display.getHeight() - 17);
		}
		else {
			if (this.playing || this.position.getSeconds() > 10) {
				this.drawProgress(graphics);
			}

			final BufferedImage button1;
			if (this.playing && !this.paused) {
				button1 = this.pause;
			}
			else {
				button1 = this.play;
			}

			graphics.drawImage(button1, $().display.getB1(), 195, null);

			if (this.modeCount > 1) {
				graphics.drawImage(this.mode, $().display.getB2(), 195, null);
			}

			if (this.currentMode != null) {
				this.currentMode.paint(graphics, this.length, this.position, this.chapters, this.chapter);
			}
		}

		$().display.refresh();
	}

	private void drawProgress(final Graphics2D graphics) {
		graphics.setColor(Color.GRAY);
		graphics.fillRect(0, 180, $().display.getWidth(), 5);

		final Double len = (double) $().display.getWidth() / (double) this.length.getSeconds() * this.position.getSeconds();
		graphics.setColor(Color.CYAN);
		graphics.fillRect(0, 180, len.intValue(), 5);
	}
}
