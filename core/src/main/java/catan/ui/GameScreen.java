package catan.ui;

import catan.SettlersOfCatan;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import enums.Colour;
import game.build.Building;
import game.build.Road;
import game.build.Settlement;
import grid.Edge;
import grid.Hex;
import grid.Node;
import grid.Port;

import java.util.List;

public class GameScreen implements Screen
{
	private static final Environment ENVIRONMENT = new Environment();
	static
	{
		ENVIRONMENT.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.8f, 0.8f, 0.8f, 1.0f));
		ENVIRONMENT.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));
	}

	private final SettlersOfCatan game;
	private final Array<ModelInstance> persistentInstances = new Array<>();
	private final Array<ModelInstance> volatileInstances = new Array<>();
	private final ModelBatch worldBatch = new ModelBatch();
	private final SpriteBatch hudBatch = new SpriteBatch();
	private final PerspectiveCamera cam;
	private final CameraController camController;
	private static final Vector3 ORIGIN = new Vector3(0, 0, 0);

	/** Initial world setup */
	GameScreen(final SettlersOfCatan game)
	{
		this.game = game;

		// camera
		cam = new PerspectiveCamera(50f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.position.set(0f, 8f, -10f);
		cam.lookAt(0, 0, 0); // look at centre of world
		cam.near = 0.01f; // closest distance to be rendered
		cam.far = 300f; // farthest distance to be rendered
		cam.update();

		// input processors
		final InputMultiplexer multiplexer = new InputMultiplexer();
		camController = new CameraController(cam);
		multiplexer.addProcessor(camController);
		multiplexer.addProcessor(new GameController(cam, game.getState()));
		Gdx.input.setInputProcessor(multiplexer);

		final CatanModelFactory factory = new CatanModelFactory(game.assets);
		persistentInstances.add(factory.getSeaInstance());
		persistentInstances.add(factory.getIslandInstance());

		for (final Hex hex : game.getState().getGrid().getHexesAsList())
		{
			persistentInstances.add(factory.getTerrainInstance(hex.getResource(), hex.get3DPos()));
		}
		// drawPorts();
	}

	@Override
	public void render(final float delta)
	{
		camController.update();
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT); // clear
																				// screen
		updateInstancesFromState();

		worldBatch.begin(cam);
		worldBatch.render(persistentInstances, ENVIRONMENT);
		worldBatch.render(volatileInstances, ENVIRONMENT);
		worldBatch.end();
	}

	private void updateInstancesFromState()
	{
		volatileInstances.clear();
		drawRoads();
		drawBuildings();
	}

	private void drawRoads()
	{
		Model model = game.assets.getModel("road.g3db");

		List<Edge> edges = game.client.getState().getGrid().getEdgesAsList();
		for (Edge edge : edges)
		{
			Road road = edge.getRoad();

			if (road != null)
			{
				Vector3 place = edge.get3dVectorMidpoint(edge);
				ModelInstance instance = new ModelInstance(model, place);

				instance.materials.get(0).set(ColorAttribute.createDiffuse(playerToColour(road.getPlayerColour())));
				instance.transform.scale(0.1f, 0.1f, 0.1f);
				Vector2 compare = edge.getX().get2DPos();
				Vector2 compareTo = edge.getY().get2DPos();

				if (compare.x != compareTo.x)
				{
					if (compare.y > compareTo.y)
					{
						instance.transform.rotate(0, 1, 0, -60f);
					}
					else
					{
						instance.transform.rotate(0, 1, 0, 60f);
					}
				}

				volatileInstances.add(instance);
			}
		}
	}

	private void drawBuildings()
	{
		Model model = game.assets.getModel("settlement.g3db");
		Model modelCity = game.assets.getModel("city.g3db");

		List<Node> nodes = game.client.getState().getGrid().getNodesAsList();
		for (Node node : nodes)
		{
			Vector3 place = node.get3DPos();
			ModelInstance instance;

			Building building = node.getBuilding();

			if (building != null)
			{
				if (building instanceof Settlement)
				{
					instance = new ModelInstance(model, place);
				}
				else
				{
					instance = new ModelInstance(modelCity, place);
				}

				instance.materials.get(0).set(ColorAttribute.createDiffuse(playerToColour(building.getPlayerColour())));
				instance.transform.scale(0.2f, 0.2f, 0.2f);

				volatileInstances.add(instance);
			}
		}
	}

	public void drawPorts()
	{
		Model model = game.assets.getModel("port.g3db");

		List<Port> ports = game.client.getState().getGrid().getPortsAsList();
		for (Port port : ports)
		{
			Vector3 n = port.getX().get3DPos();
			Vector3 n2 = port.getY().get3DPos();
			float xMidpoint = (n.x + n2.x) / 2;
			float yMidpoint = (n.y + n2.y) / 2;

			Vector3 Midpoint = new Vector3(xMidpoint, 0.1f, yMidpoint);
			ModelInstance instance = new ModelInstance(model, Midpoint);
			persistentInstances.add(instance);

		}
	}

	private static Color playerToColour(final Colour name)
	{
		switch (name)
		{
		case BLUE:
			return Color.BLUE;
		case RED:
			return Color.RED;
		case WHITE:
			return Color.WHITE;
		case ORANGE:
			return Color.ORANGE;
		default:
			return null;
		}
	}

	@Override
	public void dispose()
	{
		worldBatch.dispose();
		hudBatch.dispose();
		persistentInstances.clear();
		volatileInstances.clear();
	}

	// Required but unused
	@Override public void resize(final int width, final int height) {}
	@Override public void pause() {}
	@Override public void resume() {}
	@Override public void hide() {}
	@Override public void show() {}
}
