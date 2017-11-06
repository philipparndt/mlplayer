package de.rnd7.mp3player.mplayer;

public abstract class MPlayerCommand<T> {

	private final MPlayerThread player;

	public MPlayerCommand(final MPlayerThread player) {
		this.player = player;
	}

	public MPlayerThread getPlayer() {
		return this.player;
	}

	public abstract T execute();


}
