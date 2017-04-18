package catan;

import AI.LocalAIClient;
import AI.RemoteAIClient;
import catan.ui.AssetMan;
import catan.ui.SplashScreen;
import catan.ui.hud.HeadsUpDisplay;
import client.Client;
import client.ClientGame;
import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Window;

public class SettlersOfCatan extends com.badlogic.gdx.Game
{
	private static Skin skin;
	private static final AssetMan assets = new AssetMan();
	public Client client;
	private Thread t;
	private boolean active;
	private HeadsUpDisplay hud;
	private boolean isAI;

	public static Skin getSkin()
	{
		return skin;
	}

	public static AssetMan getAssets()
	{
		return assets;
	}

	@Override
	public void create()
	{
		Gdx.app.setLogLevel(Application.LOG_DEBUG);
		Gdx.graphics.setContinuousRendering(false);

		skin = new Skin((Gdx.files.internal("skin.json")));
		skin.add("default",
				new Window.WindowStyle(skin.getFont("display"), Color.BLACK, AssetMan.getDrawable("dialog.png")));
		skin.add("default", new CheckBox.CheckBoxStyle(AssetMan.getDrawable("checkBoxOff.png"),
				AssetMan.getDrawable("checkBoxOn.png"), skin.getFont("body"), Color.WHITE));

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
	 * @return the status of the connection
	 */
	public void startNewRemoteClient(Client c)
	{
		if (c instanceof RemoteAIClient)
		{
			isAI = true;
		}

		client = c;
		t = new Thread(client);
		t.start();
	}

	/**
	 * Starts up a new server and local client
	 */
	public void startNewServer(Client c)
	{
		if (c instanceof LocalAIClient)
		{
			isAI = true;
		}

		client = c;
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

	public void setHUD(final HeadsUpDisplay hud)
	{
		this.hud = hud;
	}

	public void sendMessage(final String message)
	{
		hud.sendMessage(message);
	}

	public void showDiscard()
	{
		Gdx.app.postRunnable(() -> hud.showDiscardDialog());
	}

	public void showResponse()
	{
		Gdx.app.postRunnable(() -> hud.showResponse());
	}

	public void showChooseResource()
	{
		Gdx.app.postRunnable(() -> hud.showChooseResource());
	}

	public boolean isAI()
	{
		return isAI;
	}
}
