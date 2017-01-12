package catan;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.Texture;

import board.*;

import java.awt.Point;
import java.util.Hashtable;

public class SettlersOfCatan extends ApplicationAdapter
{
	HexGrid map;
	Texture texture;
	OrthographicCamera camera;
	ShapeRenderer sr;

	@Override
	public void create()
	{
		camera = new OrthographicCamera(480, 320);
		camera.position.set(240.0f, 160.0f, 0.0f);
		camera.update();

		sr = new ShapeRenderer();
		sr.setAutoShapeType(true);

		map = new HexGrid();

		//camera.position.set(map.getWidth() / 2.0f, map.getHeight() / 2.0f, 0.0f);
	}

	@Override
	public void render()
	{
		camera.update();
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);


		sr.begin();
		Hashtable<Point, Hex> hexes = map.grid;
		for (Point p : hexes.keySet()) {
			float[] verticies = new float[12];

			for (int i = 0; i < 6; i++) {
			  float vx = 16 * (float)Math.cos(2 * Math.PI * i / 6) * (float)p.getX() + 300;
			  float vy = 16 * (float)Math.sin(2 * Math.PI * i / 6) * (float)p.getY() + 300;
			  verticies[2*i] = vx;
			  verticies[2*i+1] = vy;
			}

			sr.polygon(verticies);
		}

		sr.end();
	}

	@Override
	public void resize(int w, int h)
	{
		camera.viewportWidth = w;
		camera.viewportHeight = h;
	}
}
