package catan.ui;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.sun.org.apache.regexp.internal.RE;
import grid.BoardElement;
import grid.Edge;
import grid.Hex;
import grid.Node;
import intergroup.Requests;
import intergroup.board.Board;

import java.util.List;
import java.util.concurrent.Semaphore;


/**
 * Handles mouse input intended to interact with the game board.
 */
class GameController implements InputProcessor
{
	private final Camera camera;

	private final List<Hex> hexes;
	private final List<Node> nodes;
	private final List<Edge> edges;

    /** A plane parallel to the game board used to detect clicks. */
	private final static Plane DETECTION_PLANE = new Plane(new Vector3(0, 1, 0), 0.1f);

    GameController(GameScreen screen)
	{
		this.camera = screen.cam;
		this.hexes = screen.game.getState().getGrid().getHexesAsList();
		this.nodes = screen.game.getState().getGrid().getNodesAsList();
		this.edges = screen.game.getState().getGrid().getEdgesAsList();
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button)
	{
		Ray ray = camera.getPickRay(screenX, screenY);

		Vector3 intersectionPoint = new Vector3();
		if (!Intersector.intersectRayPlane(ray, DETECTION_PLANE, intersectionPoint))
			return false;

		BoardElement element = findElement(intersectionPoint.x, intersectionPoint.z);
		if (element == null) return false;

        dealWithClick(element);

        return true;
	}

	/** Returns the clicked on element or null if none. */
	private BoardElement findElement(float planeX, float planeY) {
        BoardElement found = null;

        found = findNode(planeX, planeY);
        if (found != null) return found;

        found = findEdge(planeX, planeY);
        if (found != null) return found;

        found = findHex(planeX, planeY);
        if (found != null) return found;

        return null;
    }

    /**
     * @param planeX X-coord on {@code DETECTION_PLANE}
     * @param planeY Y-coord on {@code DETECTION_PLANE}
     * @return clicked on {@link Node} or null.
     */
    private Node findNode(float planeX, float planeY)
    {
        return null;
    }

    /**
     * @param planeX X-coord on {@code DETECTION_PLANE}
     * @param planeY Y-coord on {@code DETECTION_PLANE}
     * @return clicked on {@link Edge} or null.
     */
    private Edge findEdge(float planeX, float planeY)
    {
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

        for(Hex hex : hexes){
            final Vector3 pos = hex.getCartesian();

            final double furthestLeft = pos.x - HEX_WIDTH/2;
            final double furthestRight = pos.x + HEX_WIDTH/2;
            final double highestHeight = pos.z + ((Math.sqrt(3)*HEX_WIDTH)/4);
            final double lowestHeight = pos.z - ((Math.sqrt(3)*HEX_WIDTH)/4);

            if (planeX <= furthestRight && planeX >= furthestLeft)
            {
                if (planeY <= highestHeight && planeY >= lowestHeight)
                {
                    return hex;
                }
            }
        }
        return null;
    }

    private void dealWithClick(BoardElement element)
    {
        try {
            Semaphore lock = client.getTurnLock();
            lock.acquire();

            modTurn(element);

            client.sendTurn();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        element.materials.get(0).set(ColorAttribute.createDiffuse(Color.BLACK));
    }

    private void modTurn(Node node)
    {
        client.getTurn().setChosenNode(node);
        client.getTurn().setChosenMove(Requests.Request.BodyCase.BUILDSETTLEMENT);
    }

    private void modTurn(Edge edge)
    {
        client.getTurn().setChosenEdge(edge);
        client.getTurn().setChosenMove(Requests.Request.BodyCase.BUILDROAD);
    }

    private void modTurn(Hex hex)
    {
        client.getTurn().setChosenNode(hex);
        client.getTurn().setChosenMove(Requests.Request.BodyCase.MOVEROBBER);
    }

    // The InputProcessor interface requires these methods be implemented. However, we have no
    // use for them so they all return false (indicating that the input event was not dealt with by us).
	@Override public boolean keyUp(int keycode) { return false; }
	@Override public boolean keyDown(int keycode) { return false; }
	@Override public boolean keyTyped(char character) { return false; }
	@Override public boolean touchDown(int screenX, int screenY, int pointer, int button) { return false; }
	@Override public boolean touchDragged(int screenX, int screenY, int pointer) { return false; }
	@Override public boolean mouseMoved(int screenX, int screenY) { return false; }
	@Override public boolean scrolled(int amount) { return false; }
}
