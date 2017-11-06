package de.rnd7.mp3player.hw;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

public class Button {
	private final GpioPinDigitalInput input;

	public Button(final GpioPinDigitalInput input) {
		this.input = input;
	}

	static Button create(final GpioController gpio, final Pin pin) {
		final GpioPinDigitalInput input = gpio.provisionDigitalInputPin(pin, PinPullResistance.PULL_UP);
		input.setDebounce(50);

		return new Button(input);
	}

	public void onPressed(final Runnable listener) {
		this.input.addListener((GpioPinListenerDigital) event -> {
			if (event.getState().isLow()) {
				listener.run();
			}
		});
	}

	public void onReleased(final Runnable listener) {
		this.input.addListener((GpioPinListenerDigital) event -> {
			if (event.getState().isHigh()) {
				listener.run();
			}
		});
	}

}
