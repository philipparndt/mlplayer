package de.rnd7.mp3player;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.rnd7.mp3player.library.FolderLibrary;
import de.rnd7.mp3player.mplayer.MPlayer;
import de.rnd7.mp3player.mplayer.VolumeControl;
import de.rnd7.mp3player.splash.ProgressMonitorDialog;

/**
 *
 * @author philipparndt
 *
 */
public class Main {

	private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

	public Main(final String args[]) {
		try {
			final Properties properties = this.loadProperties(args);
	
			final FolderLibrary library = this.initLibrary(properties);
			final VolumeControl volumeControl = this.initVolume(properties);
			final MPlayer player = this.initMPlayer(properties);
	
			new Controller(new Viewer(), library, player, volumeControl);
			
			PlayerMainLoop.exec();
		}
		catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		}
	}

	private MPlayer initMPlayer(final Properties properties) {
		final String propertyMplayer = properties.getProperty("mplayer");
		final MPlayer player = new MPlayer(propertyMplayer);
		return player;
	}

	private FolderLibrary initLibrary(final Properties properties) throws IOException {
		final ProgressMonitorDialog splash = new ProgressMonitorDialog();
		final String propertyLibrary = properties.getProperty("library");
		final FolderLibrary library = new FolderLibrary(new File(propertyLibrary));
		library.init(splash);
		splash.completed();
		return library;
	}

	private VolumeControl initVolume(final Properties properties) {
		final String propertyMixer = properties.getProperty("mixer");
		final int mixerMin = this.toInt(properties.getProperty("mixer.min"), 1);
		final int mixerDefault = this.toInt(properties.getProperty("mixer.min"), 10);
		final int mixerMax = this.toInt(properties.getProperty("mixer.max"), 100);
		final VolumeControl volumeControl = new VolumeControl(propertyMixer, mixerMin, mixerMax);
		if (mixerDefault >= 0) {
			volumeControl.set(mixerDefault);
		}
		return volumeControl;
	}

	private int toInt(final String s, final int defaultValue) {
		if (s == null) {
			return defaultValue;
		}

		try {
			return Integer.parseInt(s);
		}
		catch (final NumberFormatException e) {
			return defaultValue;
		}
	}

	private Properties loadProperties(final String args[]) throws IOException {
		final Properties result = new Properties();

		if (args.length == 1) {
			try (InputStream input = new FileInputStream(new File(args[0]))) {
				result.load(input);
			}
		}
		else {
			try (InputStream input = Main.class.getResourceAsStream("config.properties")) {
				result.load(input);
			}
		}

		return result;
	}

	public static void main(final String args[]) throws Exception {
		new Main(args);
	}
}
