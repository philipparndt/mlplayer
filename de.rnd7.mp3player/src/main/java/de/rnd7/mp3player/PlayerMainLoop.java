package de.rnd7.mp3player;

import static de.rnd7.mp3player.hw.HW.$;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pi4j.io.gpio.GpioFactory;

public class PlayerMainLoop {
	private static final Logger LOGGER = LoggerFactory.getLogger(PlayerMainLoop.class);
	
	private boolean running = true;

	
	private PlayerMainLoop() {
		mainLoop();
	}
	
	public static void exec() {
		new PlayerMainLoop();
	}
	
	public void mainLoop() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				running = false;
			}
		});
		
		try {
			while(running) {
				Thread.sleep(500);
			}
		}
		catch (final InterruptedException e) {
			LOGGER.info(e.getMessage(), e);
			
			Thread.currentThread().interrupt();
		}
		finally {
			$().display.clear();

			GpioFactory.getInstance().shutdown();
		}
	}
}
