/*

 *	This file is the JNI Java part of a Raspberry Pi FrameBuffer project.
 *
 *	Created 2013 by Thomas Welsch (ttww@gmx.de).
 *
 *	Do whatever you want to do with it :-)
 *
 **/

package org.tw.pi.framebuffer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.concurrent.ArrayBlockingQueue;

import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class is the Java front end for a simple to use FrameBuffer driver.
 * Simple draw in the BufferedImage and all changes are transfered to the FrameBuffer device.<p>
 * For testing purpose a dummy device is supported (via the devicename "dummy_160x128" instead of "/dev/fb1").<p<
 * The Java process needs write access to the frame buffer device file.
 * <p/>
 * It's used to drive small bit mapped screens connected via SPI, see
 * http://www.sainsmart.com/blog/ada/
 * <p/>
 * <p/>
 * My Linux kernel config for SPI display was:
 * <pre>
 * CONFIG_FB_ST7735=y
 * CONFIG_FB_ST7735_PANEL_TYPE_RED_TAB=y
 * CONFIG_FB_ST7735_RGB_ORDER_REVERSED=y
 * CONFIG_FB_ST7735_MAP=y
 * CONFIG_FB_ST7735_MAP_RST_GPIO=25
 * CONFIG_FB_ST7735_MAP_DC_GPIO=24
 * CONFIG_FB_ST7735_MAP_SPI_BUS_NUM=0
 * CONFIG_FB_ST7735_MAP_SPI_BUS_CS=0
 * CONFIG_FB_ST7735_MAP_SPI_BUS_SPEED=16000000
 * CONFIG_FB_ST7735_MAP_SPI_BUS_MODE=0
 * </pre>
 * CONFIG_FB_ST7735_MAP_SPI_BUS_SPEED gives faster updates :-)
 * <p/>
 * If you get the wrong colors, try the CONFIG_FB_ST7735_RGB_ORDER_REVERSED option !
 */
public class FrameBuffer {

	private static final Logger LOGGER = LoggerFactory.getLogger(FrameBuffer.class);

	private static final int FPS = 40;        // Max. update rate

	private final String deviceName;

	private long deviceInfo;        // Private data from JNI C

	private final int width, height;
	private int bits;

	private BufferedImage img;
	private int[] imgBuffer;

	private ManualRepaintThread	mrt;
	
	private final Object deviceMutex = new Object();

	// -----------------------------------------------------------------------------------------------------------------

	private native long openDevice(String device);

	private native void closeDevice(long di);

	private native int getDeviceWidth(long di);

	private native int getDeviceHeight(long di);

	private native int getDeviceBitsPerPixel(long di);

	private native boolean updateDeviceBuffer(long di, int[] buffer);

	static {
		// System.loadLibrary("libFrameBufferJNI"); // FrameBufferJNI.dll (Windows) or FrameBufferJNI.so (Unixes)
		System.load("/usr/lib/jni/libFrameBufferJNI.so"); // during runtime. .DLL within .JAR
	}


	// -----------------------------------------------------------------------------------------------------------------

	/**
	 * Open the named frame buffer device and starts the automatic update thread between the internal
	 * BufferedImage and the device.
	 *
	 * @param deviceName e.g. /dev/fb1 or dummy_320x200
	 */
	public FrameBuffer(final String deviceName) {
		this(deviceName, true);
	}

	// -----------------------------------------------------------------------------------------------------------------

	/**
	 * Open the named frame buffer device.
	 *
	 * @param deviceName e.g. /dev/fb1 or dummy_320x200
	 * @param autoUpdate if true, starts the automatic update thread between the internal
	 *                   BufferedImage and the device. If false, you have to call repaint();
	 */
	public FrameBuffer(final String deviceName, final boolean autoUpdate) {

		this.deviceName = deviceName;

		this.deviceInfo = this.openDevice(deviceName);

		if (Math.abs(this.deviceInfo) < 10) {
			throw new IllegalArgumentException("Init. for frame buffer " + deviceName + " failed with error code " + this.deviceInfo);
		}

		this.width  = this.getDeviceWidth(this.deviceInfo);
		this.height = this.getDeviceHeight(this.deviceInfo);

		LOGGER.info("Open with {} ({})", deviceName, this.deviceInfo);
		LOGGER.info("  width   {}", this.getDeviceWidth(this.deviceInfo));
		LOGGER.info("  height  {}", this.getDeviceHeight(this.deviceInfo));
		LOGGER.info("  bpp     {}", this.getDeviceBitsPerPixel(this.deviceInfo));
		
		// We always use ARGB image type.
		this.img = new BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_ARGB);
		this.imgBuffer = ((DataBufferInt) this.img.getRaster().getDataBuffer()).getBankData()[0];

		if (autoUpdate) {
			new AutoUpdateThread().start();
		} else {
			this.mrt = new ManualRepaintThread();
			this.mrt.start();
		}
	}

	// -----------------------------------------------------------------------------------------------------------------

	private ScreenPanel screenPanel;

	/**
	 * Returns a ScreenPanel (JPanel) which represents the actual frame buffer device.
	 *
	 * @return ScreenPanel...
	 */
	public ScreenPanel getScreenPanel() {
		synchronized (deviceMutex) {
			if (this.screenPanel != null) {
				throw new IllegalStateException("Only one screen panel supported");
			}

			this.screenPanel = new ScreenPanel();

			return this.screenPanel;
		}
	}

	// -----------------------------------------------------------------------------------------------------------------

	/**
	 * Internal helper class for displaying the current frame buffer image via a JPanel.
	 */
	@SuppressWarnings("serial")
	public class ScreenPanel extends JPanel {

		private int scale = 1;

		public ScreenPanel() {
			this.setPreferredSize(new Dimension(FrameBuffer.this.width, FrameBuffer.this.height));
		}

		@Override
		protected void paintComponent(final Graphics g) {
			synchronized (FrameBuffer.this.getUpdateLockForSync()) {
				super.paintComponent(g);

				final int w  = this.getWidth();
				final int h  = this.getHeight();
				final int wi = FrameBuffer.this.img.getWidth() * this.scale;
				final int hi = FrameBuffer.this.img.getHeight() * this.scale;

				final Graphics2D g2 = (Graphics2D) g;
				//				g2.translate(w / 2 - wi / 2, h / 2 - hi / 2);
				g2.translate((w - wi) / 2, (h - hi) / 2);
				g2.scale(this.scale, this.scale);

				g.setColor(Color.BLACK);
				g.fillRect(0, 0, FrameBuffer.this.img.getWidth(), FrameBuffer.this.img.getHeight());
				g.drawImage(FrameBuffer.this.img, 0, 0, null);
			}
		}

		public void setScale(final int scale) {
			this.scale = scale;
			this.repaint();
		}

		public int componentToScreenX(int x) {
			final int w =  this.getWidth();
			final int wi = FrameBuffer.this.img.getWidth();
			final int d = (int) ((w - wi * this.scale) / 2f);
			x = (x - d) / this.scale;
			if (x <  0) {
				x = 0;
			}
			if (x >= wi) {
				x = wi - 1;
			}
			return x;
		}
		public int componentToScreenY(int y) {
			final int h = this.getHeight();
			final int hi = FrameBuffer.this.img.getHeight();
			final int d = (int) ((h - hi * this.scale) / 2f);
			y = (y - d) / this.scale;
			if (y <  0) {
				y = 0;
			}
			if (y >= hi) {
				y = hi - 1;
			}
			return y;
		}
	}

	// -----------------------------------------------------------------------------------------------------------------

	/**
	 * Internal helper class for refreshing the frame buffer display and/or JPanel.
	 */
	private class AutoUpdateThread extends Thread {

		AutoUpdateThread() {
			this.setDaemon(true);
			this.setName("FB " + FrameBuffer.this.deviceName + " update");
		}

		@Override
		public void run() {
			final int SLEEP_TIME = 1000 / FPS;

			LOGGER.trace("Run Update");
			
			while (FrameBuffer.this.deviceInfo != 0) {

				FrameBuffer.this.updateScreen();

				try {
					sleep(SLEEP_TIME);
				} catch (final InterruptedException e) {
					LOGGER.trace(e.getMessage(), e);
					Thread.currentThread().interrupt();
					break;
				}

			}    // while
		}

	}    // class UpdateThread

	// -----------------------------------------------------------------------------------------------------------------

	private final ArrayBlockingQueue<Boolean> repaintQueue = new ArrayBlockingQueue<>(1);

	/**
	 * Request an repaint manually. This method can called at high frequencies. An internal repaint tread is used to
	 * avoid exceeding the FPS value.
	 */
	@SuppressWarnings("squid:S899")
	public void repaint() {
		if (this.mrt == null) {
			throw new IllegalStateException("automatic repaint is active, no need to call this");
		}
		this.repaintQueue.offer(Boolean.TRUE);
	}

	/**
	 * Internal helper class for refreshing the frame buffer display and/or JPanel.
	 */
	private class ManualRepaintThread extends Thread {

		ManualRepaintThread() {
			this.setDaemon(true);
			this.setName("FB " + FrameBuffer.this.deviceName + " repaint");
		}

		@Override
		public void run() {
			final int SLEEP_TIME = 1000 / FPS;

			try {
				while (FrameBuffer.this.deviceInfo != 0) {

					FrameBuffer.this.repaintQueue.take();
					FrameBuffer.this.updateScreen();

					sleep(SLEEP_TIME);

				}    // while
			} catch (final InterruptedException e) {
				LOGGER.trace(e.getMessage(), e);
				Thread.currentThread().interrupt();
			}

		}    // class UpdateThread
	}

	// -----------------------------------------------------------------------------------------------------------------

	/**
	 * Returns the BufferedImage for drawing. Anything your draw here is synchronized to the frame buffer.
	 *
	 * @return BufferedImage of type ARGB.
	 */
	public BufferedImage getScreen() {
		return this.img;
	}

	// -----------------------------------------------------------------------------------------------------------------

	/**
	 * Close the device.
	 */
	public void close() {
		synchronized (deviceMutex) {
			this.closeDevice(this.deviceInfo);
			this.deviceInfo = 0;
			this.img = null;
			this.imgBuffer = null;
		}
	}

	// -----------------------------------------------------------------------------------------------------------------

	private long	lastUpdate;
	private	int		updateCount;

	/**
	 * Update the screen if no automatic sync is used (see constructor autoUpdate flag).
	 * This method is normally called by the autoUpdate thread and is not limited about any frame rate.
	 *
	 * @return true if the BufferedImage was changed since the last call.
	 */
	public boolean updateScreen() {


		synchronized (deviceMutex) {
			if (this.deviceInfo == 0) {
				return false;
			}

			boolean ret;
			synchronized (this.updateLock) {

				ret = this.updateDeviceBuffer(this.deviceInfo, this.imgBuffer);

				this.updateCount++;
				if (this.lastUpdate == 0) {
					this.lastUpdate = System.currentTimeMillis();
				}
				final long now = System.currentTimeMillis();

				final long diff = now - this.lastUpdate;

				if (diff >= 1000) {
					this.updateCount = 0;
					this.lastUpdate  = now;
				}

			}

			if (ret && this.screenPanel != null) {
				this.screenPanel.repaint();
			}
			return ret;
		}    // sync
	}

	// -----------------------------------------------------------------------------------------------------------------

	private final Object updateLock = new Object();

	public Object getUpdateLockForSync() {
		return this.updateLock;
	}

	// -----------------------------------------------------------------------------------------------------------------

	public int getWidth() {
		return this.width;
	}

	// -----------------------------------------------------------------------------------------------------------------

	public int getHeight() {
		return this.height;
	}

	// -----------------------------------------------------------------------------------------------------------------

}    // of class