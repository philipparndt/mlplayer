package de.rnd7.mp3player.library;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

public class LibraryItem {
	private final File file;
	private Optional<Image> cover;
	private int currentPosition=0;
	private int length=0;

	public LibraryItem(final File file) {
		this.file = file;

		this.load();
	}

	private void load() {
		final File statusFile = this.getStatusFile();
		if (statusFile.exists()) {
			try {
				final String content = FileUtils.readFileToString(statusFile, "utf8");
				final Pattern pattern = Pattern.compile("(\\d+);(\\d+)");
				final Matcher matcher = pattern.matcher(content);

				if (matcher.matches()) {
					this.currentPosition = Integer.parseInt(matcher.group(1));
					this.length = Integer.parseInt(matcher.group(2));
				}
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void replay() {
		if (this.currentPosition + 20 > this.length) {
			this.currentPosition = 0;
		}
	}

	public File getFile() {
		return this.file;
	}

	public void setCover(final Image cover) {
		this.cover = Optional.of(cover);
	}

	public Optional<Image> getCover() {
		return this.cover;
	}

	public void setPosition(final int currentPosition, final int length) {
		this.currentPosition = currentPosition;
		this.length = length;

		try {
			final File statusFile = this.getStatusFile();
			FileUtils.writeStringToFile(statusFile, currentPosition + ";" + length, "utf8");
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	public int getCurrentPosition() {
		this.replay();

		return this.currentPosition;
	}

	public int getLength() {
		return this.length;
	}

	private File getStatusFile() {
		return new File(this.file.getParentFile(), this.file.getName() + ".ml.status");
	}
}
