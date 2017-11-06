package de.rnd7.mp3player.hw;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import org.tw.pi.framebuffer.FrameBuffer;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.RaspiPin;

public class Display {
	public final FrameBuffer fb;
	public final BufferedImage screen;
	public final Graphics2D graphics;

	public final int width = 320;
	public final int height = 240;

	public final int b1 = 5;
	public final int b2 = 95;
	public final int b3 = 185;
	public final int b4 = 275;

	public final BacklightControl backlight;

	Display(final String device) {
		this.fb = new FrameBuffer(device, false);
		this.screen = this.fb.getScreen();
		this.graphics = this.screen.createGraphics();

		final GpioController controller = GpioFactory.getInstance();
		this.backlight = new BacklightControl(controller.provisionPwmOutputPin(RaspiPin.GPIO_01));
	}

	public BufferedImage getScreen() {
		return this.screen;
	}

	public FrameBuffer getFb() {
		return this.fb;
	}

	public Graphics2D getGraphics() {
		return this.graphics;
	}

	public void refresh() {
		this.fb.updateScreen();
	}

	public void clear() {
		this.graphics.setColor(Color.BLACK);
		this.graphics.fillRect(0, 0, 320, 240);
		this.refresh();
	}

}
