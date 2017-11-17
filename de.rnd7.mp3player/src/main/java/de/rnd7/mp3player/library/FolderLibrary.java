package de.rnd7.mp3player.library;

import static de.rnd7.mp3player.hw.HW.$;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.rnd7.mp3player.mp4.CoverGernerator;
import de.rnd7.mp3player.mp4.MP4CoverExtractor;
import de.rnd7.mp3player.splash.ProgressMonitor;

public class FolderLibrary {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(FolderLibrary.class);

	private final File folder;
	private List<LibraryItem> items = new ArrayList<>();
	private int currentIndex = -1;

	public FolderLibrary(final File folder) {
		this.folder = folder;
	}

	public void init(final ProgressMonitor monitor) {
		this.items = Stream.of(this.folder.listFiles())
				.filter(this::isAudioFile)
				.sorted(Comparator.comparing(File::getName))
				.map(LibraryItem::new)
				.collect(Collectors.toList());

		this.initCovers(monitor);

		this.currentIndex = 0;
	}

	private void initCovers(final ProgressMonitor monitor) {
		int i = 0;
		for (final LibraryItem item : this.items) {
			monitor.setProgress(i++, this.items.size(), item.getFile().getName());
			this.initCover(item);
		}
	}

	private void initCover(final LibraryItem item) {
		final File file = item.getFile();
		final File coverFile = new File(file + ".cover.png");

		if (coverFile.exists()) {
			try {
				item.setCover(load(coverFile));
			} catch (final IOException e) {
				LOGGER.error("Error loading cover {}", coverFile, e);
			}
		}
		else {
			LOGGER.info("Updating cover for " + file);

			final CoverGernerator gernerator = new CoverGernerator($().display.getWidth(), $().display.getHeight());

			final Optional<BufferedImage> newCover = MP4CoverExtractor.extract(file).map(gernerator::generateCover);
			if (newCover.isPresent()) {
				final BufferedImage cover = newCover.get();
				item.setCover(cover);

				try {
					ImageIO.write(cover, "png", coverFile);
				} catch (final IOException e) {
					LOGGER.error("Error loading cover {}", coverFile, e);
				}
			}
			else {
				// TODO persist that we have not found any cover
			}
		}
	}


	private static BufferedImage load(final File file) throws IOException {
		try (InputStream in = new FileInputStream(file)) {
			return ImageIO.read(in);
		}
	}

	private boolean isAudioFile(final File file) {
		final String name = file.getName().toLowerCase();
		return name.endsWith(".mp3") || name.endsWith(".m4b") || name.endsWith(".m4a");
	}

	public void next() {
		this.currentIndex++;

		if (this.currentIndex == this.items.size()) {
			this.currentIndex = 0;
		}
	}

	public void previous() {
		this.currentIndex--;

		if (this.currentIndex < 0) {
			this.currentIndex = this.items.size() - 1;
		}
	}

	public Optional<LibraryItem> getCurrentItem() {
		if (this.currentIndex < 0) {
			return Optional.empty();
		}
		return Optional.of(this.items.get(this.currentIndex));
	}


}
