package de.rnd7.mp3player.mp4;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.awt.image.RescaleOp;

public class CoverGernerator {
	private final int width;
	private final int height;

	public CoverGernerator(final int width, final int height) {
		this.width = width;
		this.height = height;
	}

	public BufferedImage generateCover(final BufferedImage input) {
		final Image cover = scale(input, Math.min(this.width, this.height));

		final BufferedImage bufferedImage = toBufferedImage(cover);

		final int findYStart = this.findYStart(bufferedImage);
		final int findYEnd = this.findYEnd(bufferedImage);
		final int findXStart = this.findXStart(bufferedImage);
		final int findXEnd = this.findXEnd(bufferedImage);

		final int width = findXEnd - findXStart;
		final int height = findYEnd - findYStart;

		final BufferedImage toBeBlurred = bufferedImage.getSubimage(findXStart, findYStart, width, height);

		final Image background = toBeBlurred.getScaledInstance(this.width, this.height, Image.SCALE_FAST);

		BufferedImage blurred = this.blur(toBufferedImage(background), 40);
		this.darken(blurred);
		blurred = blurred.getSubimage(40, 40, this.width - 40*2, this.height-40*2);

		final BufferedImage result = new BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_ARGB);
		final Graphics2D graphics = result.createGraphics();

		graphics.drawImage(blurred, 0, 0, this.width, this.height, null);
		graphics.drawImage(cover, (this.width - cover.getWidth(null)) / 2, 0, null);

		return result;
	}
	
	Rectangle2D findBounds(final BufferedImage input) {
		final int yStart = this.findYStart(input);
		final int yEnd = this.findYEnd(input);
		final int xStart = this.findXStart(input);
		final int xEnd = this.findXEnd(input);

		return new Rectangle2D.Double(xStart, yStart, xEnd - xStart, yEnd - yStart);
	}

	private int findYStart(final BufferedImage image) {
		final int end = image.getHeight() / 2;
		for (int y = 0; y < end; y++) {
			if (this.isColoredYLine(image, y)) {
				return y;
			}
		}
		return 0;
	}

	private int findYEnd(final BufferedImage image) {
		final int end = image.getHeight() / 2;
		for (int y = image.getHeight() - 1; y > end; y--) {
			if (this.isColoredYLine(image, y)) {
				return y + 1;
			}
		}
		return image.getHeight();
	}

	private int findXStart(final BufferedImage image) {
		final int end = image.getWidth() / 2;
		for (int x = 0; x < end; x++) {
			if (this.isColoredXLine(image, x)) {
				return x;
			}
		}
		return 0;
	}

	private int findXEnd(final BufferedImage image) {
		final int end = image.getWidth() / 2;
		for (int x = image.getWidth() - 1; x > end; x--) {
			if (this.isColoredXLine(image, x)) {
				return x + 1;
			}
		}
		return image.getWidth();
	}

	private boolean isColoredYLine(final BufferedImage image, final int y) {
		int colored = 0;

		for (int x = 0; x <image.getWidth(); x++) {
			if (this.isColored(image.getRGB(x,y))) {
				colored++;
			}
		}

		return colored > image.getWidth() * 0.2;
	}

	private boolean isColoredXLine(final BufferedImage image, final int x) {
		int colored = 0;

		for (int y = 0; y <image.getHeight(); y++) {
			if (this.isColored(image.getRGB(x,y))) {
				colored++;
			}
		}

		return colored > image.getWidth() * 0.2;
	}

	private boolean isColored(final int rgb) {
		final int  red   = (rgb & 0x00ff0000) >> 16;
		final int  green = (rgb & 0x0000ff00) >> 8;
		final int  blue  =  rgb & 0x000000ff;

		final float[] hsbvals = new float[3];
		Color.RGBtoHSB(red, green, blue, hsbvals );

		//		float h = hsbvals[0];
		final float s = hsbvals[1];
		final float b = hsbvals[2];

		return s >=0.5f && b>=0.5f;
	}

	private void darken(final BufferedImage input) {
		final RescaleOp rescaleOp = new RescaleOp(0.7f, 15, null);
		rescaleOp.filter(input, input);
	}

	private BufferedImage blur(final BufferedImage input, final int radius) {
		final int size = radius * 2 + 1;
		final float weight = 1.0f / (size * size);
		final float[] data = new float[size * size];

		for (int i = 0; i < data.length; i++) {
			data[i] = weight;
		}

		final Kernel kernel = new Kernel(size, size, data);
		final ConvolveOp op = new ConvolveOp(kernel, ConvolveOp.EDGE_ZERO_FILL, null);
		return op.filter(input, null);
	}

	private static Image scale(final BufferedImage image, final int maxSize) {
		final int width = image.getWidth();
		final int height = image.getHeight();

		final int targetWidth;
		final int targetHeight;

		if (width >= height) {
			targetWidth = maxSize;
			final Double h = (double) targetWidth/width * height;
			targetHeight =h.intValue();
		}
		else {
			targetHeight = maxSize;
			final Double h = (double) targetHeight/height * width;
			targetWidth =h.intValue();
		}


		return image.getScaledInstance(targetWidth, targetHeight, Image.SCALE_DEFAULT);
	}

	public static BufferedImage toBufferedImage(final Image image) {
		if (image instanceof BufferedImage) {
			return (BufferedImage) image;
		}

		final BufferedImage result = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);

		final Graphics2D bGr = result.createGraphics();
		bGr.drawImage(image, 0, 0, null);
		bGr.dispose();

		return result;
	}


}
