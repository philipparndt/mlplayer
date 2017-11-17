package de.rnd7.mp3player.hw;

import static de.rnd7.mp3player.hw.HW.$;

import java.time.Duration;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BacklightTimerThread extends Thread {

	private static final Logger LOGGER = LoggerFactory.getLogger(BacklightTimerThread.class);
	
	private static final Duration DURATION = Duration.ofSeconds(60);

	private LocalDateTime onUntil;

	@Override
	public void run() {
		this.reset();

		try {
			while(true) {
				Thread.sleep(1000);

				if (this.onUntil.isBefore(LocalDateTime.now())) {
					$().display.backlight.off();
				}
			}
		}
		catch (final InterruptedException e) {
			LOGGER.debug(e.getMessage(), e);
			
			Thread.currentThread().interrupt();
		}
	}


	public boolean reset() {
		final boolean wasOn = $().display.backlight.isTurnedOn();

		$().display.backlight.on();
		this.onUntil = LocalDateTime.now().plus(DURATION);

		return wasOn;
	}

	public static BacklightTimerThread createStarted() {
		final BacklightTimerThread result = new BacklightTimerThread();
		result.start();
		return result;
	}
}
