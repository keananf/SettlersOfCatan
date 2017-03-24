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

class GameController implements InputProcessor
{
	private final Camera camera;

	private final List<Hex> hexes;
	private final List<Node> nodes;
	private final List<Edge> edges;

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

		BoardElement inst = findElement(intersectionPoint.x, intersectionPoint.z);
		if (inst == null)
		{
			return false;
		}
		else
		{
			Hex selected = screen.game.client.getState().getGrid().grid.get(inst.catanCoord);
			try {
				Semaphore lock = screen.game.client.getTurnLock();
				lock.acquire();
				screen.game.client.getTurn().setChosenHex(selected);
				screen.game.client.getTurn().setChosenMove(Requests.Request.BodyCase.BODY_NOT_SET);
				lock.release();
				screen.game.client.sendTurn();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			inst.materials.get(0).set(ColorAttribute.createDiffuse(Color.BLACK));
			return true;
		}
	}

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

    private Node findNode(float planeX, float planeY)
    {
        return null;
    }

    private Edge findEdge(float planeX, float planeY)
    {
        return null;
    }

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

	@Override public boolean keyUp(int keycode) { return false; }
	@Override public boolean keyDown(int keycode) { return false; }
	@Override public boolean keyTyped(char character) { return false; }
	@Override public boolean touchDown(int screenX, int screenY, int pointer, int button) { return false; }
	@Override public boolean touchDragged(int screenX, int screenY, int pointer) { return false; }
	@Override public boolean mouseMoved(int screenX, int screenY) { return false; }
	@Override public boolean scrolled(int amount) { return false; }
}
