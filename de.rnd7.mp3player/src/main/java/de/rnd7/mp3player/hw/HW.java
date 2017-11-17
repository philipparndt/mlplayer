package de.rnd7.mp3player.hw;

public class HW {
	private static final HW instance = new HW();

	public final Display display = new Display("/dev/fb1");
	public final Buttons buttons = new Buttons();

	private HW() {

	}

	@SuppressWarnings("squid:S00100")
	public static HW $() {
		return instance;
	}

}
