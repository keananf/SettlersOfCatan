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
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.graphics.g3d.attributes.IntAttribute;
import com.badlogic.gdx.utils.Array;

import enums.ResourceType;

import board.Hex;
import catan.SettlersOfCatan;

public class GameScreen implements Screen
{
	final private AssMan assets = new AssMan();
	final private ModelBatch MODEL_BATCH = new ModelBatch();

	final private Camera cam;
	final private CatanCamController camController;
	final private CameraInputController debugController;

	final private Array<ModelInstance> boardInstances = new Array<ModelInstance>();
	final private Environment environment = new Environment();

	final private SettlersOfCatan game;

	public GameScreen(final SettlersOfCatan game)
	{
		this.game = game;

		initCamera();
		initCameraController();
		initBoard();
		initEnvironment();
	}

	private void initCamera()
	{
		cam = new PerspectiveCamera(50f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.position.set(10f, 8f, 0f);
		cam.lookAt(0, 0, 0); // look at centre of world
		cam.near = 0.01f; // closest things to be rendered
		cam.far = 300f; // furthest things to be rendered
		cam.update();
	}

	private void initCameraController()
	{
		camController = new CatanCamController(cam);
		debugController = new CameraInputController(cam);
		Gdx.input.setInputProcessor(debugController);
	}

	private void initBoard()
	{
		for(Entry<Point, Hex> coord : game.state.getGrid().grid.entrySet())
		{
			ModelInstance hex = new ModelInstance(assets.getModel("hex.g3db"), hexPointToCartVec(coord.getKey()));
			boardInstances.add(hex);

			Model resourceModel = null;
			switch(coord.getValue().getResource())
			{
				case Brick:
					resourceModel = assets.getModel("claymine.g3db");
					break;
				case Lumber:
					resourceModel = assets.getModel("grass.g3db");
					break;
				case Generic:
					resourceModel = assets.getModel("desert.g3db");
					break;
				case Wool:
					resourceModel = assets.getModel("grass.g3db");
					break;
				case Grain:
					resourceModel = assets.getModel("grain.g3db");
					break;
				case Ore:
					resourceModel = assets.getModel("mountain.g3db");
					break;
				default:
					assert(false);
			}

			Vector3 resourcePos = hexPointToCartVec(coord.getKey());
			resourcePos.y += 0.1f;
			ModelInstance resource = new ModelInstance(resourceModel, resourcePos);;
			boardInstances.add(resource);
		}
	}

	private void initEnvironment()
	{
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.8f, 0.8f, 0.8f, 1.0f));
		environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));
	}

	private static final Vector3 hexPointToCartVec(Point p)
	{
		final float x = (float)(p.getX());
		final float y = (float)(p.getY());
		return new Vector3(x, 0f, (float)((2*y-x)/Math.sqrt(3)));
	}

	@Override
	public void render(final float delta)
	{
		Gdx.gl.glClearColor(0/255, 128/255, 255/255, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		camController.update();

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
	@Override public void resize(final int width, final int height) {}
	@Override public void pause() {}
	@Override public void resume() {}
	@Override public void hide() {}
	@Override public void show() {}
}
