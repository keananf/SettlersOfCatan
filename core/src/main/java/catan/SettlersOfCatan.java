package catan;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.Texture;

import server.Server;

import java.awt.Point;
import java.util.Hashtable;

public class SettlersOfCatan extends ApplicationAdapter
{
	Server serv = new Server();

	@Override
	public void create()
	{
		(new Thread(serv)).start();
	}

	@Override
	public void render()
	{
	}

	@Override
	public void resize(int w, int h)
	{
	}
}
