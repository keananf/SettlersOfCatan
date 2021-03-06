package catan.ui;

import client.Client;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import grid.BoardElement;
import grid.Edge;
import grid.Hex;
import grid.Node;

import java.util.List;

/**
 * Handles mouse input intended to interact with the game board.
 */
class GameController implements InputProcessor
{
	private final Camera camera;
	private final MoveBuilder moveBuilder;

	private final List<Hex> hexes;
	private final List<Edge> edges;

	/** A plane parallel to the game board used to detect clicks. */
	private final static float DETECTION_Y = 0.6f;
	private final static Plane DETECTION_PLANE = new Plane(new Vector3(0, 2f, 0), new Vector3(0, DETECTION_Y, 0));

	GameController(final Camera camera, Client client)
	{
		this.camera = camera;
		this.hexes = client.getState().getGrid().getHexesAsList();
		List<Node> nodes = client.getState().getGrid().getNodesAsList();
		this.edges = client.getState().getGrid().getEdgesAsList();
		this.moveBuilder = new MoveBuilder(client);
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button)
	{
		Ray ray = camera.getPickRay(screenX, screenY);
		Vector3 intersectionPoint = new Vector3();
		if (!Intersector.intersectRayPlane(ray, DETECTION_PLANE, intersectionPoint)) return false;

		BoardElement element = findElement(intersectionPoint.x, intersectionPoint.z);

		if (element == null) return false;

		moveBuilder.onSelect(element);

		return true;
	}

	/** Returns the clicked on element or null if none. */

	private BoardElement findElement(float planeX, float planeY)
	{
		BoardElement found;

		found = findNode(planeX, planeY);
		if (found != null) return found;

		found = findEdge(planeX, planeY);
		if (found != null) return found;

		found = findHex(planeX, planeY);
		if (found != null) return found;

		System.out.println("NULL");
		return null;
	}

	private Hex getHex(float planeX, float planeY)
	{
		for (Hex hex : hexes)
		{
			final Vector2 pos = hex.get2DPos();

			if (pos.dst(planeX, planeY) < 2)
			{
				System.out.println("HEX");
				return hex;
			}

		}
		return null;
	}

	/**
	 * @param planeX X-coord on {@code DETECTION_PLANE}
	 * @param planeY Y-coord on {@code DETECTION_PLANE}
	 * @return clicked on {@link Node} or null.
	 */
	private Node findNode(float planeX, float planeY)
	{
		System.out.println(planeX + "" + planeY);
		Hex h = getHex(planeX, planeY);

		if (h == null) return null;

		for (Node node : h.getNodes())
		{
			Vector2 coord = node.get2DPos();
			if (coord.dst(planeX, planeY) < 0.3) { return node; }
		}

		return null;
	}

	/**
	 * @param planeX X-coord on {@code DETECTION_PLANE}
	 * @param planeY Y-coord on {@code DETECTION_PLANE}
	 * @return clicked on {@link Edge} or null.
	 */
	private Edge findEdge(float planeX, float planeY)
	{
		for (Edge edge : edges)
		{
			Vector2 nodeX = edge.getX().get2DPos();
			Vector2 nodeY = edge.getY().get2DPos();

			float x = (nodeX.x + nodeY.x) / 2;
			float y = (nodeX.y + nodeY.y) / 2;
			Vector2 check = new Vector2(x, y);
			if (check.dst(planeX, planeY) < 0.3) { return edge; }
		}

		return null;

	}

	/**
	 * @param planeX X-coord on {@code DETECTION_PLANE}
	 * @param planeY Y-coord on {@code DETECTION_PLANE}
	 * @return clicked on {@link Hex} or null.
	 */
	private Hex findHex(float planeX, float planeY)
	{
		final float HEX_WIDTH = 2f;
		for (Hex hex : hexes)
		{
			final Vector2 pos = hex.get2DPos();

		 final double furthestLeft = pos.x - HEX_WIDTH / 2;
		 final double furthestRight = pos.x + HEX_WIDTH / 2;
		 final double highestHeight =  pos.y + ((Math.sqrt(3) * HEX_WIDTH) / 4);
		 final double lowestHeight = pos.y - ((Math.sqrt(3) * HEX_WIDTH) / 4);

		 if (planeX <= furthestRight && planeX >= furthestLeft)
		 {
		 	if (planeY <= highestHeight && planeY >= lowestHeight)
		 	{
		 		return hex;
		 	}
		 }
		} return null;
	}

	// The InputProcessor interface requires these methods be implemented.
	// However, we have no
	// use for them so they all return false (indicating that the input event
	// was not dealt with by us).
	@Override
	public boolean keyUp(int keycode)
	{
		return false;
	}

	@Override
	public boolean keyDown(int keycode)
	{
		return false;
	}

	@Override
	public boolean keyTyped(char character)
	{
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button)
	{
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer)
	{
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY)
	{
		return false;
	}

	@Override
	public boolean scrolled(int amount)
	{
		return false;
	}
}
