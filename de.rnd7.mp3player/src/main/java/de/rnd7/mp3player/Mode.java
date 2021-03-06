package de.rnd7.mp3player;

import static de.rnd7.mp3player.hw.HW.$;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.time.Duration;
import java.util.Optional;

public class Mode {

	private Optional<Runnable> commandL = Optional.empty();
	private Optional<Runnable> commandR = Optional.empty();
	private final String name;
	private BufferedImage left;
	private BufferedImage right;

	public Mode(final String name) {
		this.name = name;
	}

	public Mode setCommandL(final BufferedImage image, final Runnable runnable) {
		this.left = image;
		this.commandL = Optional.of(runnable);
		return this;
	}

	public Mode setCommandR(final BufferedImage image, final Runnable runnable) {
		this.right = image;
		this.commandR = Optional.of(runnable);
		return this;
	}

	public Optional<Runnable> getCommandL() {
		return this.commandL;
	}

	public Optional<Runnable> getCommandR() {
		return this.commandR;
	}

	public void paint(final Graphics2D graphics, final Duration length, final Duration position, int chapters, int chapter) {
		graphics.drawImage(this.left, $().display.getB3(), 195, null);
		graphics.drawImage(this.right, $().display.getB4(), 195, null);
	}

	@Override
	public String toString() {
		return this.name;
	}
	
	public boolean isPlaying(final Duration length, final Duration position) {
		return (!(length.isNegative() || position.isNegative() || length.getSeconds() == Long.MAX_VALUE)); 
	}

}
