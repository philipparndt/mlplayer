package de.rnd7.mp3player;

class CancelabelCommand implements Runnable{
	private boolean cancelled = false;
	private final Runnable command;

	public CancelabelCommand(final Runnable command) {
		this.command = command;
	}

	public void cancel() {
		this.cancelled = true;
	}

	@Override
	public void run() {
		if (!this.cancelled) {
			this.command.run();
		}
	}
}