package de.rnd7.mp3player.hw;

import com.pi4j.io.gpio.GpioPinPwmOutput;

public class BacklightControl {
	private final GpioPinPwmOutput pwm;
	private boolean turnedOn = false;

	public BacklightControl(final GpioPinPwmOutput pwm) {
		this.pwm = pwm;

		this.on();
	}

	public void off() {
		if (this.turnedOn) {
			this.pwm.setPwm(0);
			this.turnedOn = false;
		}
	}

	public void on() {
		if (!this.turnedOn) {
			this.pwm.setPwm(1024);
			this.turnedOn = true;
		}
	}

	public void toggle() {
		if (this.turnedOn) {
			this.off();
		}
		else {
			this.on();
		}
	}

	public boolean isTurnedOn() {
		return this.turnedOn;
	}
}
