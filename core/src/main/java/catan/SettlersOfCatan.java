package catan;

import AI.LocalAIClient;
import AI.RemoteAIClient;
import catan.ui.SplashScreen;
import client.Client;
import client.ClientGame;
import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import enums.Difficulty;

public class SettlersOfCatan extends com.badlogic.gdx.Game
{
	public Skin skin;
	public Client client;
	private Thread t;
	private boolean active;

	@Override
	public void create()
	{
		Gdx.app.setLogLevel(Application.LOG_DEBUG);
		Gdx.graphics.setContinuousRendering(false);

		skin = new Skin(Gdx.files.internal("skin.json"));

		// start off at the splash screen
		setScreen(new SplashScreen(this));
		active = true;
	}

	@Override
	public void render()
	{
		super.render();
	}

	@Override
	public void dispose()
	{
		if (active)
		{
			client.log("Shutdown Client", "Shutting down.");
			active = false;
			skin.dispose();
			if (client != null && client.isActive()) client.shutDown();
			client = null;
			try
			{
				if (t != null) t.join();
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
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
		t = new Thread(client);
		t.start();
		return ((RemoteAIClient) client).isInitialised();
	}

	/**
	 * Starts up a new server and local client
	 */
	public void startNewServer()
	{
		client = new LocalAIClient(Difficulty.EASY,this);
		t = new Thread(client);
		t.start();
	}

	/**
	 * @return the client's gamestate object
	 */
	public ClientGame getState()
	{
		boolean val = false;

		// Block until the game board is received.
		client.log("Client Setup", "Waiting for Game Information....");
		while (true)
		{
			if (val) break;
			try
			{
				client.getStateLock().acquire();
				try
				{
					if (client.getState() != null) val = true;
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

	public boolean isActive()
	{
		return active;
	}
}
