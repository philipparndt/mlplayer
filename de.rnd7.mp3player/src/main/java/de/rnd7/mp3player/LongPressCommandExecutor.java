package de.rnd7.mp3player;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class LongPressCommandExecutor {
	private static final int COMMAND_RATE = 100;
	private static final int INITIAL_DELAY = 1000;

	private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);
	private ScheduledFuture<?> future;
	private CancelabelCommand cancelabelCommand;

	public synchronized void schedule(final Runnable command) {
		command.run(); // Execute once

		this.cancel();
		this.cancelabelCommand = new CancelabelCommand(command);

		this.future = this.executor.scheduleAtFixedRate(this.cancelabelCommand, INITIAL_DELAY, COMMAND_RATE, TimeUnit.MILLISECONDS);

	}

	public synchronized void cancel() {
		if (this.cancelabelCommand != null) {
			this.cancelabelCommand.cancel();
		}

		if (this.future != null) {
			this.future.cancel(false);
		}
	}
}
