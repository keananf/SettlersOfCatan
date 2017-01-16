package catan;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import server.Server;
import catan.ui.SplashScreen;

public class SettlersOfCatan extends Game
{
	public Skin skin;
	final private Server serv = new Server();

	@Override
	public void create()
	{
		Gdx.app.setLogLevel(Application.LOG_DEBUG);
		skin = new Skin(Gdx.files.internal("skin.json"));

		// Start a game server in its own thread
		(new Thread(serv)).start();

		// start off at the splash screen
		setScreen(new SplashScreen(this));
	}

	@Override
	public void render()
	{
		super.render();
	}

	@Override
	public void dispose()
	{
		skin.dispose();
	}
}
