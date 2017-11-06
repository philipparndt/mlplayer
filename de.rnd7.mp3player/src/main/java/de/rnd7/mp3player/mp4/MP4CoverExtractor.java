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

public class MP4CoverExtractor {
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
			e.printStackTrace();
		}

		return Optional.empty();
	}

	private static Optional<BufferedImage> load(final AppleCoverBox coverBox) {
		try(final InputStream in = new ByteArrayInputStream(coverBox.getCoverData())) {
			return Optional.of(ImageIO.read(in));
		}
		catch (final IOException e) {
			e.printStackTrace();
			return Optional.empty();
		}
	}

	private static  List<AppleCoverBox> findCoverBox(final BasicContainer container) {
		return container.getBoxes(AppleCoverBox.class, true);
	}
}
