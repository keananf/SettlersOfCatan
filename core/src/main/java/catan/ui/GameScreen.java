package catan.ui;

import java.awt.Point;
import java.util.Map.Entry;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.attributes.IntAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.utils.Array;

import enums.ResourceType;

import board.Hex;
import catan.SettlersOfCatan;

public class GameScreen implements Screen
{
	final private AssMan assets = new AssMan();
	final private ModelBatch MODEL_BATCH = new ModelBatch();

	protected Camera cam;
	private CatanCamController camController;
	private GameController gameController;

	final protected Array<GameObject> objs = new Array<>();
	final protected Array<GameObject> hexes = new Array<>();
	final private Environment environment = new Environment();

	final private SettlersOfCatan game;

	public GameScreen(final SettlersOfCatan game)
	{
		this.game = game;

		initCamera();
		camController = new CatanCamController(cam);
		gameController = new GameController(this);

		final InputMultiplexer multiplexer = new InputMultiplexer();
		multiplexer.addProcessor(camController);
		multiplexer.addProcessor(gameController);
		Gdx.input.setInputProcessor(multiplexer);

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

	private void initBoard()
	{
		final ModelBuilder builder = new ModelBuilder();
		final long attributes = Usage.Position | Usage.Normal | Usage.TextureCoordinates;

		// sea
		final Material water = new Material(TextureAttribute.createDiffuse(new Texture(Gdx.files.internal("textures/water.jpg"))));
		final Model sea = builder.createCylinder(150f, 0.01f, 150f, 6, water, attributes);
		objs.add(new GameObject(sea, new Vector3(0, -1, 0)));

		// hex tiles
		final Material dirt = new Material(TextureAttribute.createDiffuse(new Texture(Gdx.files.internal("textures/dirt.png"))));
		final Model hex = builder.createCylinder(2f, 0.2f, 2f, 6, dirt, attributes);

		for (Entry<Point, Hex> coord : game.state.getGrid().grid.entrySet())
		{
			final GameObject instance = new GameObject(hex, hexPointToCartVec(coord.getKey()));
			instance.transform.rotate(0, 1, 0, 90f);

			final Color colour;
			switch (coord.getValue().getResource())
			{
				case Grain:   colour = Color.YELLOW;    break;
				case Wool:    colour = Color.WHITE;     break;
				case Ore:     colour = Color.GRAY;      break;
				case Brick:   colour = Color.FIREBRICK; break;
				case Lumber:  colour = Color.FOREST;    break;
				case Generic: colour = Color.ORANGE;    break;
				default:      colour = null;            break;
			}
			instance.materials.get(0).set(ColorAttribute.createDiffuse(colour));

			objs.add(instance);
			hexes.add(instance);
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
		MODEL_BATCH.render(objs, environment);
		MODEL_BATCH.end();
	}

	@Override
	public void dispose()
	{
		assets.dispose();
		MODEL_BATCH.dispose();
		objs.clear();
	}

	// Required but unused
	@Override public void resize(final int width, final int height) {}
	@Override public void pause() {}
	@Override public void resume() {}
	@Override public void hide() {}
	@Override public void show() {}
}
