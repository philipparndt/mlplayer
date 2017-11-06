package de.rnd7.mp3player.hw;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.RaspiPin;

public class Buttons {
	public final Button button1;
	public final Button button2;
	public final Button button3;
	public final Button button4;

	Buttons() {
		final GpioController controller = GpioFactory.getInstance();

		this.button1 = Button.create(controller, RaspiPin.GPIO_00);
		this.button2 = Button.create(controller, RaspiPin.GPIO_03);
		this.button3 = Button.create(controller, RaspiPin.GPIO_04);
		this.button4 = Button.create(controller, RaspiPin.GPIO_02);
	}

	public Button getButton1() {
		return this.button1;
	}

	public Button getButton2() {
		return this.button2;
	}

	public Button getButton3() {
		return this.button3;
	}

	public Button getButton4() {
		return this.button4;
	}
}
