package catan.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import catan.SettlersOfCatan;

public class DesktopLauncher {
	private static final int INITIAL_WINDOW_WIDTH = 1024;
	private static final int INITIAL_WINDOW_HEIGHT = 576;

	public static void main (String[] arg) {
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.setTitle("Settlers of Catan");
		config.setWindowedMode(INITIAL_WINDOW_WIDTH, INITIAL_WINDOW_HEIGHT);
		config.setWindowSizeLimits(3*INITIAL_WINDOW_WIDTH/4, INITIAL_WINDOW_HEIGHT, -1, -1);

		new Lwjgl3Application(new SettlersOfCatan(), config);
	}
}
