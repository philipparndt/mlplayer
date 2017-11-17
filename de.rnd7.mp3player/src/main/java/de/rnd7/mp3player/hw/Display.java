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

	private static final int WIDTH = 320;
	private static final int HEIGHT = 240;

	private static final int B1 = 5;
	private static final int B2 = 95;
	private static final int B3 = 185;
	private static final int B4 = 275;

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
	
	/**
	 * 
	 * @return the x position of button 1 
	 */
	public int getB1() {
		return B1;
	}
	/**
	 * 
	 * @return the x position of button 2 
	 */
	public int getB2() {
		return B2;
	}
	
	/**
	 * 
	 * @return the x position of button 3
	 */
	public int getB3() {
		return B3;
	}
	
	/**
	 * 
	 * @return the x position of button 4 
	 */
	public int getB4() {
		return B4;
	}
	
	/**
	 * 
	 * @return the display width in pixels
	 */
	public int getWidth() {
		return WIDTH;
	}
	
	/**
	 * 
	 * @return the display height in pixels
	 */
	public int getHeight() {
		return HEIGHT;
	}

}
