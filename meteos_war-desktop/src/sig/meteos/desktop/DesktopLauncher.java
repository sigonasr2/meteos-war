package sig.meteos.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import sig.meteos.MeteosWar;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = MeteosWar.SCREEN_WIDTH;
		config.height = MeteosWar.SCREEN_HEIGHT;
		new LwjglApplication(new MeteosWar(), config);
	}
}
