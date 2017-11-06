package de.rnd7.mp3player.splash;

public interface ProgressMonitor {
	void setProgress(final int current, final int total, String title);
}
