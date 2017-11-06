package de.rnd7.mp3player;

import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

final class SystemOutGPIOListener implements GpioPinListenerDigital {
	@Override
	public void handleGpioPinDigitalStateChangeEvent(final GpioPinDigitalStateChangeEvent event) {
		System.out.println(" --> GPIO PIN STATE CHANGE: " + event.getPin() + " = " + event.getState());
	}
}