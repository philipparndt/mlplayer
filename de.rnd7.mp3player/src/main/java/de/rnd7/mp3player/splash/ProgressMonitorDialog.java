package de.rnd7.mp3player.splash;

import static de.rnd7.mp3player.hw.HW.$;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

public class ProgressMonitorDialog implements ProgressMonitor {
	private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

	private final BufferedImage splash;
	private int current = -1;
	private int total = -1;

	private String title;

	public ProgressMonitorDialog() throws IOException {
		this.splash = load("splash.png");

		this.executor.scheduleAtFixedRate(this::refresh, 0, 500, TimeUnit.MILLISECONDS);
	}

	@Override
	public void setProgress(final int current, final int total, final String title) {
		this.current = current;
		this.total = total;
		this.title = title;
	}

	public void completed() {
		this.executor.shutdownNow();
	}

	public void refresh() {
		final Graphics2D graphics = $().display.graphics;

		graphics.setColor(Color.BLACK);
		graphics.fillRect(0, 0, 320, 240);

		graphics.drawImage(this.splash, 0, 0, null);

		if (this.total >= 0) {
			this.drawProgress(graphics);
		}

		$().display.refresh();
	}

	private void drawProgress(final Graphics2D graphics) {
		graphics.setColor(Color.WHITE);
		graphics.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 8));

		if (this.title != null) {
			graphics.drawString(this.title, 0, $().display.getHeight() - 10);
		}

		final Double len = (double) $().display.getWidth() / (double) this.total * this.current;
		graphics.setColor(Color.CYAN);
		graphics.fillRect(0, $().display.getHeight() - 5, len.intValue(), 5);
	}

	private static BufferedImage load(final String name) throws IOException {
		try (InputStream in = ProgressMonitorDialog.class.getResourceAsStream(name)) {
			return ImageIO.read(in);
		}
	}

}
