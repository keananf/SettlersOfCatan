package catan.ui;

import java.awt.Point;
import java.util.Map.Entry;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

import board.Hex;
import catan.SettlersOfCatan;

public class GameScreen implements Screen
{
	final private AssMan assets = new AssMan();
	final private ModelBatch MODEL_BATCH = new ModelBatch();

	final private Camera cam;
	final private SpinCamController camController;

	final private Array<ModelInstance> boardInstances = new Array<ModelInstance>();
	final private Environment environment;

	final private SettlersOfCatan game;

	public GameScreen(final SettlersOfCatan game)
	{
		this.game = game;

		// init camera
		cam = new PerspectiveCamera(75f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.position.set(0f, 7f, 10f);
		cam.lookAt(0, 0, 0);
		cam.near = 0.1f;
		cam.far = 200;
		cam.update();

		// init camera controller
		camController = new SpinCamController(cam);
		Gdx.input.setInputProcessor(camController);

		// init external assets
		initBoard();

		// init environment
		environment = new Environment();
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.8f, 0.8f, 0.8f, 1.0f));
	}

	private void initBoard()
	{
		assets.load("models/hex.g3db", Model.class);
		assets.finishLoading();

		for(Entry<Point, Hex> coord : game.state.getGrid().grid.entrySet())
		{
			float x = (float)(coord.getKey().getX());
			float y = (float)(coord.getKey().getY());
			Vector3 pos = new Vector3(
					(float)(x + (y*Math.cos(Math.PI/3))),
					0f,
					(float)(y*Math.cos(Math.PI/6) - (x*Math.sin(Math.PI/6))));

			ModelInstance hex = new ModelInstance(
					assets.getModel("hex.g3db"),
					pos);

			boardInstances.add(hex);
		}
	}

	@Override
	public void render(final float delta)
	{
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		camController.update();
		renderBoard();
	}

	private void renderBoard()
	{
		MODEL_BATCH.begin(cam);
		MODEL_BATCH.render(UITools.getAxesInst());
		MODEL_BATCH.render(boardInstances, environment);
		MODEL_BATCH.end();
	}

	@Override
	public void resize(final int width, final int height)
	{
	}

	@Override
	public void dispose()
	{
		assets.dispose();
		MODEL_BATCH.dispose();
		boardInstances.clear();
	}

	// Required but unused
	@Override public void pause() {}
	@Override public void resume() {}
	@Override public void hide() {}
	@Override public void show() {}
}
