package de.rnd7.mp3player;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

final class LoggingGPIOListener implements GpioPinListenerDigital {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(LoggingGPIOListener.class);
	
	@Override
	public void handleGpioPinDigitalStateChangeEvent(final GpioPinDigitalStateChangeEvent event) {
		LOGGER.info(" --> GPIO PIN STATE CHANGE: " + event.getPin() + " = " + event.getState());
	}
}