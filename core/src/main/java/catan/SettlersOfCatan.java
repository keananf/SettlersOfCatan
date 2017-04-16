package catan;

import catan.ui.AssMan;
import catan.ui.SplashScreen;
import catan.ui.hud.HeadsUpDisplay;
import client.Client;
import client.ClientGame;
import client.LocalClient;
import client.RemoteClient;
import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Window;

public class SettlersOfCatan extends com.badlogic.gdx.Game
{
	private static Skin skin;
	private static AssMan assets = new AssMan();
	public Client client;
	private Thread t;
	private boolean active;
	private HeadsUpDisplay hud;

	public static Skin getSkin() {
		return skin;
	}

	public static AssMan getAssets() {
		return assets;
	}

	@Override
	public void create()
	{
		Gdx.app.setLogLevel(Application.LOG_DEBUG);
		Gdx.graphics.setContinuousRendering(false);

		skin = new Skin(Gdx.files.internal("skin.json"));
		Window.WindowStyle ws = new Window.WindowStyle(SettlersOfCatan.getSkin().getFont("body"), Color.WHITE, AssMan.getDrawable("resources.png"));
		SettlersOfCatan.getSkin().add("default", ws);

		// start off at the splash screen
		setScreen(new SplashScreen(this));
		active = true;
	}

	@Override
	public void dispose()
	{
		skin.dispose();

		System.exit(1);
		if (active)
		{
			active = false;
			client.log("Shutdown Client", "Shutting down.");
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
		client = new RemoteClient(host, this);
		t = new Thread(client);
		t.start();
		return ((RemoteClient) client).isInitialised();
	}

	/**
	 * Starts up a new server and local client
	 */
	public void startNewServer()
	{
		client = new LocalClient(this);
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
		return client.getState();
	}

	public boolean isActive()
	{
		return active;
	}

	public void setHUD(HeadsUpDisplay hud)
	{
		this.hud = hud;
	}

	public void showDiscard()
	{
		hud.showDiscardDialog();
	}

    public void showResponse()
	{
		hud.showResponse();
    }
}
