package catan.ui;

import catan.SettlersOfCatan;
import catan.ui.hud.HeadsUpDisplay;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import enums.ResourceType;
import game.build.Building;
import game.build.Road;
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
	private final PerspectiveCamera camera;
	private final CameraController camController;
	private final HeadsUpDisplay hud;

	/** Initial world setup */
	GameScreen(final SettlersOfCatan game)
	{
		this.game = game;

		// camera
		camera = new PerspectiveCamera(50f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		camera.position.set(0f, 8f, -10f); // look from down negative z-axis
		camera.lookAt(0, 0, 0); // look at centre of world
		camera.update();

		// input processors
		final InputMultiplexer multiplexer = new InputMultiplexer();
		camController = new CameraController(camera);
		hud = new HeadsUpDisplay(game.getState().getPlayer());
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

		for (final Port port : game.getState().getGrid().getPortsAsList())
        {
            persistentInstances.add(ModelFactory.getPortInstance(port));
        }
	}

	@Override
	public void render(final float delta)
	{
		camController.update();
		updateInstancesFromState();

		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT); // clear screen

		worldBatch.begin(camera);
		worldBatch.render(persistentInstances, ENVIRONMENT);
		worldBatch.render(volatileInstances, ENVIRONMENT);
		worldBatch.end();

		hud.render();
	}

	private void updateInstancesFromState()
	{
		volatileInstances.clear();

        for (final Node node : game.getState().getGrid().getNodesAsList())
        {
            final Building building = node.getBuilding();
            if (building != null)
            {
                volatileInstances.add(ModelFactory.getBuildingInstance(building));
            }
        }

        for (final Edge edge : game.getState().getGrid().getEdgesAsList())
        {
            final Road road = edge.getRoad();
            if (road != null)
            {
                volatileInstances.add(ModelFactory.getRoadInstance(road));
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

	@Override public void pause() {}
	@Override public void resume() {}
	@Override public void hide() {}
	@Override public void show() {}
}
