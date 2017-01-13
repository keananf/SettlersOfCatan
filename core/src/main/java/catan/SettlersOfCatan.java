package catan;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.Texture;

import server.Server;
import catan.ui.MainMenuScreen;

public class SettlersOfCatan extends Game
{
	public SpriteBatch batch;

	private Server serv = new Server();

	@Override
	public void create()
	{
		// Start a game server in its own thread
		(new Thread(serv)).start();

		batch = new SpriteBatch();
		setScreen(new MainMenuScreen(this));
	}

	@Override
	public void render()
	{
		super.render();
	}

	@Override
	public void dispose()
	{
		batch.dispose();
	}
}
