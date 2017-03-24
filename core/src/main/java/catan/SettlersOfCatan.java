package catan;

import AI.RemoteAIClient;
import catan.ui.SplashScreen;
import client.Client;
import client.ClientGame;
import client.LocalClient;
import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public class SettlersOfCatan extends com.badlogic.gdx.Game
{
	public Skin skin;
	public Client client;

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
	 * 
	 * @param host the host server to connect to
	 * @return the status of the connection
	 */
	public boolean startNewRemoteClient(String host)
	{
		client = new RemoteAIClient(host);
		return ((RemoteAIClient) client).isInitialised();
	}

	/**
	 * Starts up a new server and local client
	 */
	public void startNewServer()
	{
		client = new LocalClient();
	}

	/**
	 * @return the client's gamestate object
	 */
	public ClientGame getState()
	{
		// Block until the game board is received.
		client.log("Client Setup", "Waiting for Game Information....");
		while (true)
		{
			try
			{
				client.getStateLock().acquire();
				try
				{
					if (client.getState() != null) break;
				}
				finally
				{
					client.getStateLock().release();
				}
				Thread.sleep(100);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		client.log("Client Setup", "Received Game Information");
		return client.getState();
	}
}
