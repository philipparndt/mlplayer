package de.rnd7.mp3player.hw;

public class HW {
	private final static HW instance = new HW();

	public final Display display = new Display("/dev/fb1");
	public final Buttons buttons = new Buttons();

	private HW() {

	}

	public static HW $() {
		return instance;
	}

}
