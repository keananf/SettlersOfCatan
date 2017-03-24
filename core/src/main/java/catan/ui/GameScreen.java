package catan.ui;

import catan.SettlersOfCatan;
import client.ClientGame;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.utils.Array;
import grid.Hex;

import java.awt.*;
import java.util.Map.Entry;

public class GameScreen implements Screen
{
	final private static Vector3 ORIGIN = new Vector3(0, 0, 0);
	final private AssMan assets = new AssMan();
	final private ModelBatch MODEL_BATCH = new ModelBatch();

	protected Camera cam;
	private CatanCamController camController;
	private GameController gameController;

	final protected Array<ModelInstance> instances = new Array<>();
	final private Environment environment = new Environment();

	final protected SettlersOfCatan game;

	Stage stage;
	TextButton button;
	TextButtonStyle textButtonStyle;
	Skin skin;
	TextureAtlas buttonAtlas;
	BitmapFont font;

	// button creation
	public void create()
	{
		stage = new Stage();
		Gdx.input.setInputProcessor(stage);
		font = new BitmapFont();
		skin = new Skin();
		buttonAtlas = new TextureAtlas(Gdx.files.internal("buttons/buttons,pack"));
		skin.addRegions(buttonAtlas);
		textButtonStyle = new TextButtonStyle();
		textButtonStyle.font = font;
		textButtonStyle.up = skin.getDrawable("up-button");
		textButtonStyle.down = skin.getDrawable("down-button");
		textButtonStyle.checked = skin.getDrawable("checked-button");
		button = new TextButton("Button1", textButtonStyle);
		stage.addActor(button);

	}

	// @Override
	public void render()
	{
		stage.draw();
	}

	public GameScreen(final SettlersOfCatan game)
	{
		this.game = game;

		initCamera();
		camController = new CatanCamController(cam);
		gameController = new GameController(this);
		ClientGame gameState = game.getState();
		gameController.setUp(gameState);

		final InputMultiplexer multiplexer = new InputMultiplexer();
		multiplexer.addProcessor(camController);
		multiplexer.addProcessor(gameController);
		Gdx.input.setInputProcessor(multiplexer);

		initBoard(gameState);
		initEnvironment();
	}

	private void initCamera()
	{
		cam = new PerspectiveCamera(50f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.position.set(0f, 8f, -10f);
		cam.lookAt(0, 0, 0); // look at centre of world
		cam.near = 0.01f; // closest things to be rendered
		cam.far = 300f; // farthest things to be rendered
		cam.update();
	}

	private void initBoard(ClientGame gameState)
	{

		final ModelBuilder builder = new ModelBuilder();
		final long attributes = Usage.Position | Usage.Normal | Usage.TextureCoordinates;

		// sea
		final Material water = new Material(
				TextureAttribute.createDiffuse(new Texture(Gdx.files.internal("textures/water.jpg"))));
		final Model sea = builder.createCylinder(150f, 0.01f, 150f, 6, water, attributes);
		instances.add(new ModelInstance(sea, ORIGIN));

		// land
		final Material dirt = new Material(
				TextureAttribute.createDiffuse(new Texture(Gdx.files.internal("textures/dirt.png"))));
		final Model land = builder.createCylinder(11f, 0.1f, 11f, 6, dirt, attributes);
		final ModelInstance landInstance = new ModelInstance(land, ORIGIN);
		landInstance.materials.get(0).set(ColorAttribute.createDiffuse(Color.BLACK));
		instances.add(landInstance);

		// hex tiles
		final Model hex = builder.createCylinder(2.2f, 0.2f, 2.2f, 6, dirt, attributes);

		System.out.println("****Entering init board.****");
		for (Entry<Point, Hex> coord : gameState.getGrid().grid.entrySet())
		{
			final ModelInstance instance = new ModelInstance(hex, coord.getValue().get3DPos());
			instance.transform.rotate(0, 1, 0, 90f);

			final Color colour;
			switch (coord.getValue().getResource())
			{
			case Grain:
				colour = Color.YELLOW;
				break;
			case Wool:
				colour = Color.WHITE;
				break;
			case Ore:
				colour = Color.GRAY;
				break;
			case Brick:
				colour = Color.FIREBRICK;
				break;
			case Lumber:
				colour = Color.FOREST;
				break;
			case Generic:
				colour = Color.ORANGE;
				break;
			default:
				colour = null;
				break;
			}
			instance.materials.get(0).set(ColorAttribute.createDiffuse(colour));

			instances.add(instance);
		}
	}

	private void initEnvironment()
	{
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.8f, 0.8f, 0.8f, 1.0f));
		environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));
	}

	@Override
	public void render(final float delta)
	{
		Gdx.gl.glClearColor(0 / 255, 128 / 255, 255 / 255, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		camController.update();

		MODEL_BATCH.begin(cam);
		MODEL_BATCH.render(instances, environment);
		MODEL_BATCH.end();
	}

	@Override
	public void dispose()
	{
		assets.dispose();
		MODEL_BATCH.dispose();
		instances.clear();
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
