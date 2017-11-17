package de.rnd7.mp3player.mp4;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import javax.imageio.ImageIO;

import org.mp4parser.BasicContainer;
import org.mp4parser.Box;
import org.mp4parser.IsoFile;
import org.mp4parser.boxes.apple.AppleCoverBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MP4CoverExtractor {

	private static final Logger LOGGER = LoggerFactory.getLogger(MP4CoverExtractor.class);

	private MP4CoverExtractor() {
	}
	
	public static Optional<BufferedImage> extract(final File file) {
		try (final IsoFile isoFile = new IsoFile(file)) {
			final List<Box> boxes = isoFile.getBoxes();

			final Optional<AppleCoverBox> findAny = boxes.stream()
					.filter(AppleCoverBox.class::isInstance)
					.map(AppleCoverBox.class::cast)
					.findAny();

			final Optional<AppleCoverBox> findAny2 = boxes.stream().filter(BasicContainer.class::isInstance)
					.map(BasicContainer.class::cast)
					.map(MP4CoverExtractor::findCoverBox)
					.filter(l -> !l.isEmpty())
					.map(l -> l.get(0))
					.findAny();

			return findAny.map(Optional::of)
					.orElse(findAny2)
					.flatMap(MP4CoverExtractor::load);

		}
		catch (final Exception e) {
			LOGGER.error("Error extracting cover from {}", file, e);
		}

		return Optional.empty();
	}

	private static Optional<BufferedImage> load(final AppleCoverBox coverBox) {
		try(final InputStream in = new ByteArrayInputStream(coverBox.getCoverData())) {
			return Optional.of(ImageIO.read(in));
		}
		catch (final IOException e) {
			LOGGER.error("Error loading coverbox", e);
			return Optional.empty();
		}
	}

	private static  List<AppleCoverBox> findCoverBox(final BasicContainer container) {
		return container.getBoxes(AppleCoverBox.class, true);
	}
}
