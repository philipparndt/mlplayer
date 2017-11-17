package de.rnd7.mp3player;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
public final class Main {

	private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

	private Main() {
	}
	
	public static void main(final String args[]) throws Exception {
		try {
			final Properties properties = loadProperties(Arrays.asList(args));
	
			final FolderLibrary library = initLibrary(properties);
			final VolumeControl volumeControl = initVolume(properties);
			final MPlayer player = initMPlayer(properties);
	
			new Controller(new Viewer(), library, player, volumeControl);
			
			PlayerMainLoop.exec();
		}
		catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		}
	}
	
	private static MPlayer initMPlayer(final Properties properties) {
		final String propertyMplayer = properties.getProperty("mplayer");
		return new MPlayer(propertyMplayer);
	}

	private static FolderLibrary initLibrary(final Properties properties) throws IOException {
		final ProgressMonitorDialog splash = new ProgressMonitorDialog();
		final String propertyLibrary = properties.getProperty("library");
		final FolderLibrary library = new FolderLibrary(new File(propertyLibrary));
		library.init(splash);
		splash.completed();
		return library;
	}

	private static VolumeControl initVolume(final Properties properties) {
		final String propertyMixer = properties.getProperty("mixer");
		final int mixerMin = toInt(properties.getProperty("mixer.min"), 1);
		final int mixerDefault = toInt(properties.getProperty("mixer.min"), 10);
		final int mixerMax = toInt(properties.getProperty("mixer.max"), 100);
		final VolumeControl volumeControl = new VolumeControl(propertyMixer, mixerMin, mixerMax);
		if (mixerDefault >= 0) {
			volumeControl.set(mixerDefault);
		}
		return volumeControl;
	}

	private static int toInt(final String s, final int defaultValue) {
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

	private static Properties loadProperties(List<String> args) throws IOException {
		final Properties result = new Properties();

		if (args.size() == 1) {
			try (InputStream input = new FileInputStream(new File(args.get(0)))) {
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

}
