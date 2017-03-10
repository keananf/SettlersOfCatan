package catan;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import catan.ui.SplashScreen;
import server.Server;

public class SettlersOfCatan extends Game
{
	public Skin skin;
	private Server serv;

	@Override
	public void create()
	{
		Gdx.app.setLogLevel(Application.LOG_DEBUG);
		skin = new Skin(Gdx.files.internal("skin.json"));

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
		// TODO: shutdown server
		skin.dispose();
	}

	public void startNewServer()
	{
		serv = new Server();
		(new Thread(serv)).start();
	}
}
