package de.rnd7.mp3player.mp4;

import static org.junit.Assert.*;

import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.junit.Test;

@SuppressWarnings("squid:S3415")
public class CoverGeneratorTest {
	@Test
	public void test_cover1() throws Exception {
		CoverGernerator generator = new CoverGernerator(100, 100);

		Rectangle2D bounds = generator.findBounds(loadImage("cover1.png"));
		assertEquals(15, bounds.getX(),  0.1d);
		assertEquals(20, bounds.getY(),  0.1d);
		assertEquals(50, bounds.getWidth(),  0.1d);
		assertEquals(50, bounds.getHeight(),  0.1d);
	}
	
	@Test
	public void test_cover2() throws Exception {
		CoverGernerator generator = new CoverGernerator(100, 100);

		Rectangle2D bounds = generator.findBounds(loadImage("cover2.png"));
		assertEquals(15, bounds.getX(), 0.1d);
		assertEquals(20, bounds.getY(), 0.1d);
		assertEquals(50, bounds.getWidth(), 0.1d);
		assertEquals(50, bounds.getHeight(), 0.1d);
	}
	
	@Test
	public void test_cover3() throws Exception {
		CoverGernerator generator = new CoverGernerator(100, 100);

		Rectangle2D bounds = generator.findBounds(loadImage("cover3.png"));
		assertEquals(35, bounds.getX(), 0.1d);
		assertEquals(35, bounds.getY(), 0.1d);
		assertEquals(40, bounds.getWidth(), 0.1d);
		assertEquals(30, bounds.getHeight(), 0.1d);
	}

	private BufferedImage loadImage(String name) throws IOException {
		try(InputStream in = CoverGeneratorTest.class.getResourceAsStream(name)) {
			return ImageIO.read(in);
		}
	}
}
