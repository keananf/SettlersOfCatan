package catan;

import catan.ui.SplashScreen;
import client.Client;
import client.ClientGame;
import client.LocalClient;
import client.RemoteClient;
import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import server.Server;

public class SettlersOfCatan extends com.badlogic.gdx.Game
{
	public Skin skin;
	private Server serv;
	public ClientGame state;
	private Client client;

	@Override
	public void create()
	{
		Gdx.app.setLogLevel(Application.LOG_DEBUG);
		Gdx.graphics.setContinuousRendering(false);

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
		client.shutDown();
		skin.dispose();
	}

	/**
	 * Starts up a new remote client.
	 * @param host the host server to connect to
	 * @return the status of the connection
	 */
	public boolean startNewRemoteClient(String host)
	{
		client = new RemoteClient(host);
		state = client.getState();
		return ((RemoteClient) client).isInitialised();
	}

	/**
	 * Starts up a new server and local client
	 */
	public void startNewServer()
	{
		client = new LocalClient();
		state = client.getState();
	}
}
