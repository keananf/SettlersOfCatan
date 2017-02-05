package catan.ui;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.utils.Array;

import catan.SettlersOfCatan;

public class GameScreen implements Screen
{
	final private AssetManager ASSETS = new AssetManager();
	final private ModelBatch MODEL_BATCH = new ModelBatch();

	final private PerspectiveCamera cam;
	final private CameraInputController camController;

	final private Array<ModelInstance> boardInstances= new Array<ModelInstance>();
	final private Environment environment;

	final private SettlersOfCatan game;

	public GameScreen(final SettlersOfCatan game)
	{
		this.game = game;

		// init camera
		cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.position.set(0f, 7f, 10f);
		cam.lookAt(0, 0, 0);
		cam.near = 1f;
		cam.far = 300f;
		cam.update();

		// init camera controller
		camController = new CameraInputController(cam);
		Gdx.input.setInputProcessor(camController);

		// init external assets
		initBoard();
		initUIAssets();

		// init environment
		environment = new Environment();
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
		environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));
	}

	private void initBoard()
	{
		final String[] models = {
			"claymine",
			"desert",
			"grain",
			"grass",
			"mountain",
			"hex"
		};

		for (String model : models)
			ASSETS.load("models/"+model+".g3db", Model.class);
		ASSETS.finishLoading();

		boardInstances.add(new ModelInstance(ASSETS.get("models/hex.g3db", Model.class)));
	}

	private void initUIAssets()
	{
	}

	@Override
	public void render(final float delta)
	{
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		renderBoard();
		renderUI();
	}

	private void renderBoard()
	{
		MODEL_BATCH.begin(cam);
		MODEL_BATCH.render(boardInstances, environment);
		MODEL_BATCH.end();
	}

	private void renderUI()
	{
	}

	@Override
	public void resize(final int width, final int height)
	{
	}

	@Override
	public void dispose()
	{
		ASSETS.dispose();
		MODEL_BATCH.dispose();
		boardInstances.clear();
	}

	// Required but unused
	@Override public void pause() {}
	@Override public void resume() {}
	@Override public void hide() {}
	@Override public void show() {}
}
