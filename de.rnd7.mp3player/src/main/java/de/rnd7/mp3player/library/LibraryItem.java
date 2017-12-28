package de.rnd7.mp3player.library;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LibraryItem {
	private static final Logger LOGGER = LoggerFactory.getLogger(LibraryItem.class);

	private final File file;
	private Optional<Image> cover;
	private Duration currentPosition = Duration.ZERO;
	private Duration length = Duration.ZERO;

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
					this.currentPosition = Duration.ofSeconds(Integer.parseInt(matcher.group(1)));
					this.length = Duration.ofSeconds(Integer.parseInt(matcher.group(2)));
				}
			} catch (final IOException e) {
				LOGGER.error("Error loading status from {}", statusFile, e);
			}
		}
	}

	private void replay() {
		if (!this.currentPosition.plus(Duration.ofSeconds(20)).minus(length).isNegative()) {
			this.currentPosition = Duration.ZERO;
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

	public void setPosition(final Duration currentPosition, final Duration length) {
		this.currentPosition = currentPosition;
		this.length = length;

		final File statusFile = this.getStatusFile();
		try {
			FileUtils.writeStringToFile(statusFile, currentPosition + ";" + length, "utf8");
		} catch (final IOException e) {
			LOGGER.error("Error writing status to {}", statusFile, e);
		}
	}

	public Duration getCurrentPosition() {
		this.replay();

		return this.currentPosition;
	}

	public Duration getLength() {
		return this.length;
	}

	private File getStatusFile() {
		return new File(this.file.getParentFile(), this.file.getName() + ".ml.status");
	}
}
