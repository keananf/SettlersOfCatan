package catan.ui;

import catan.SettlersOfCatan;
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
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import grid.Hex;

import java.awt.*;
import java.util.Map.Entry;

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
		cam.far = 200f;
		cam.update();

		// init camera controller
		camController = new SpinCamController(cam);
		Gdx.input.setInputProcessor(camController);

		// init external assets
		initBoard();

		// init environment
		environment = new Environment();
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.8f, 0.8f, 0.8f, 1.0f));
		environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));
	}

	private void initBoard()
	{
		assets.load("models/hex.g3db", Model.class);
		assets.finishLoading();

		for (Entry<Point, Hex> coord : game.getState().getGrid().grid.entrySet())
		{

			ModelInstance hex = new ModelInstance(assets.getModel("hex.g3db"), hexPointToCartVec(coord.getKey()));

			boardInstances.add(hex);
		}
	}

	private static final Vector3 hexPointToCartVec(Point p)
	{
		final float x = (float) (p.getX());
		final float y = (float) (p.getY());
		return new Vector3(x, 0f, (float) ((2 * y - x) / Math.sqrt(3)));
	}

	@Override
	public void render(final float delta)
	{
		Gdx.gl.glClearColor(0 / 255, 128 / 255, 255 / 255, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		camController.update();
		renderBoard();
	}

	private void renderBoard()
	{
		MODEL_BATCH.begin(cam);
		MODEL_BATCH.render(boardInstances, environment);
		MODEL_BATCH.end();
	}

	@Override
	public void dispose()
	{
		assets.dispose();
		MODEL_BATCH.dispose();
		boardInstances.clear();
	}

	// Required but unused
	@Override
	public void resize(final int width, final int height)
	{
	}

	@Override
	public void pause()
	{
	}

	@Override
	public void resume()
	{
	}

	@Override
	public void hide()
	{
	}

	@Override
	public void show()
	{
	}
}
