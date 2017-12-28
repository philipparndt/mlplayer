package de.rnd7.mp3player.mplayer;

public abstract class MPlayerPropertyCommand<T> {

	private final MPlayerThread player;

	public MPlayerPropertyCommand(final MPlayerThread player) {
		this.player = player;
	}

	public MPlayerThread getPlayer() {
		return this.player;
	}

	public abstract T read();

	public void write(T value) {
		// Do nothing
	}

}
