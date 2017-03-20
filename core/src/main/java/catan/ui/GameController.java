package catan.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.math.Vector3;

class GameController implements InputProcessor
{
	private final GameScreen screen;
	private final static Plane PLANE = new Plane(new Vector3(0, 1, 0), 0.1f);

	public GameController(GameScreen screen)
	{
		this.screen = screen;
	}

	@Override public boolean touchUp(int screenX, int screenY, int pointer, int button)
	{
		Ray ray = screen.cam.getPickRay(screenX, screenY);

		Vector3 intersectionPoint = new Vector3();
		if (!Intersector.intersectRayPlane(ray, PLANE, intersectionPoint))
			return false;

		GameObject inst = getObject(intersectionPoint.x, intersectionPoint.z);
		if (inst == null)
		{
			return false;
		}
		else
		{
			inst.materials.get(0).set(ColorAttribute.createDiffuse(Color.BLACK));
			return true;
		}
	}

	public GameObject getObject(float planeX, float planeY) {

		for(GameObject hex : screen.hexes){
			final float WIDTH = 2f;
			double furthestLeft = hex.centre.x - WIDTH/2;
			double furtherstRight = hex.centre.x + WIDTH/2;
			double heighestHeight = hex.centre.z + ((Math.sqrt(3)*WIDTH)/4);
			double lowestHeight = hex.centre.z - ((Math.sqrt(3)*WIDTH)/4);

			if (planeX <= furtherstRight && planeX >= furthestLeft)
			{
				if (planeY <= heighestHeight && planeY >= lowestHeight)
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
