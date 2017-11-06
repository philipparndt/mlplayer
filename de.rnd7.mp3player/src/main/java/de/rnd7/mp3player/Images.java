package de.rnd7.mp3player;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

public class Images {
	static BufferedImage load(final String name) {
		try (InputStream in = Images.class.getResourceAsStream(name)) {
			return ImageIO.read(in);
		}
		catch (final IOException e) {
			throw new IllegalStateException("Resource not found: " + name, e);
		}
	}
}
