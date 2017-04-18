package catan.ui;

import catan.SettlersOfCatan;
import catan.ui.hud.HeadsUpDisplay;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.utils.Array;
import enums.ResourceType;
import game.build.Building;
import game.build.Road;
import grid.Edge;
import grid.Hex;
import grid.Node;

import java.util.List;

public class GameScreen implements Screen
{
	private static final Environment ENVIRONMENT = new Environment();
	static
	{
		ENVIRONMENT.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.8f, 0.8f, 0.8f, 1.0f));
		ENVIRONMENT.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));
	}

	private final Array<ModelInstance> persistentInstances = new Array<>();
	private final Array<ModelInstance> volatileInstances = new Array<>();
	private final ModelBatch worldBatch = new ModelBatch();
	private final PerspectiveCamera camera;
	private final CameraController camController;
	private final HeadsUpDisplay hud;

	private final List<Node> nodes;
	private final List<Edge> edges;
	private final List<Hex> hexes;

	/** Initial world setup */
	GameScreen(final SettlersOfCatan game)
	{
		nodes = game.getState().getGrid().getNodesAsList();
		edges = game.getState().getGrid().getEdgesAsList();
		hexes = game.getState().getGrid().getHexesAsList();

		// camera
		camera = new PerspectiveCamera(50f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		camera.position.set(0f, 8f, -10f); // look from down negative z-axis
		camera.lookAt(0, 0, 0); // look at centre of world
		camera.update();

		// input processors
		final InputMultiplexer multiplexer = new InputMultiplexer();
		camController = new CameraController(camera);
		hud = new HeadsUpDisplay(game.client, game.isAI());
		game.setHUD(hud);
		multiplexer.addProcessor(camController);
		multiplexer.addProcessor(hud);
		multiplexer.addProcessor(new GameController(camera, game.client));
		Gdx.input.setInputProcessor(multiplexer);

		// add 3D models that won't change during gameplay
		persistentInstances.add(ModelFactory.getSeaInstance());
		persistentInstances.add(ModelFactory.getIslandInstance());

		for (final Hex hex : game.getState().getGrid().getHexesAsList())
		{
			persistentInstances.add(ModelFactory.getHexInstance(hex));
			if (!hex.getResource().equals(ResourceType.Generic))
			{
				persistentInstances.add(ModelFactory.getChitInstance(hex));
			}
		}

		hud.sendMessage("Welcome!");
	}

	@Override
	public void render(final float delta)
	{
		camController.update();
		updateInstancesFromState();

		// clear screen
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		worldBatch.begin(camera);
		worldBatch.render(persistentInstances, ENVIRONMENT);
		worldBatch.render(volatileInstances, ENVIRONMENT);
		worldBatch.end();

		hud.render(delta);
	}

	private void updateInstancesFromState()
	{
		volatileInstances.clear();

		for (final Node node : nodes)
		{
			final Building building = node.getBuilding();
			if (building != null)
			{
				volatileInstances.add(ModelFactory.getBuildingInstance(building));
			}
		}

		for (final Edge edge : edges)
		{
			final Road road = edge.getRoad();
			if (road != null)
			{
				volatileInstances.add(ModelFactory.getRoadInstance(road));
			}
		}

		for (final Hex hex : hexes)
		{
			if (hex.hasRobber())
			{
				volatileInstances.add(ModelFactory.placeRobber(hex));
			}
		}

	}

	@Override
	public void dispose()
	{
		worldBatch.dispose();
		persistentInstances.clear();
		volatileInstances.clear();
	}

	@Override
	public void resize(final int width, final int height)
	{
		camera.viewportWidth = width;
		camera.viewportHeight = height;
		camera.update();

		hud.getViewport().update(width, height, true);
	}

	@Override
	public void pause()
	{}

	@Override
	public void resume()
	{}

	@Override
	public void hide()
	{}

	@Override
	public void show()
	{}
}
