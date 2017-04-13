package catan.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;

import java.util.HashSet;

public final class CameraController implements InputProcessor
{
	private static final Vector3 ORIGIN = new Vector3(0f, 0f, 0f);
	private static final Vector3 Y_AXIS = new Vector3(0f, 1f, 0f);
	private static final float SPIN_RATE = 4f;
	private static final float ZOOM_RATE = 0.9f;
	private static final float MAX_DIST = 20f;
	private static final float MIN_DIST = 5f;
	private static final int SCROLL_UP = 1;
	private static final int SCROLL_DOWN = -1;
	private static final int SCROLL_NONE = 0;

	private final Camera camera;

	private final HashSet<Integer> heldKeys = new HashSet<>();
	private int scrollDirection = SCROLL_NONE;

	CameraController(final Camera camera)
	{
		this.camera = camera;
	}

	void update()
	{
		boolean changed = false;

		if (heldKeys.contains(Keys.LEFT))
		{
			camera.rotateAround(ORIGIN, Y_AXIS, -SPIN_RATE);
			changed = true;
		}
		if (heldKeys.contains(Keys.RIGHT))
		{
			camera.rotateAround(ORIGIN, Y_AXIS, SPIN_RATE);
			changed = true;
		}

		if (heldKeys.contains(Keys.UP)) changed |= zoomIn();
		if (heldKeys.contains(Keys.DOWN)) changed |= zoomOut();
		if (scrollDirection == SCROLL_UP) changed |= zoomOut();
		if (scrollDirection == SCROLL_DOWN) changed |= zoomIn();

		if (changed)
		{
			Gdx.graphics.requestRendering();
			camera.update();
		}
	}

	@Override
	public boolean keyDown(int keycode)
	{
		heldKeys.add(keycode);
		return true;
	}

	@Override
	public boolean keyUp(int keycode)
	{
		heldKeys.remove(keycode);
		return true;
	}

	@Override
	public boolean scrolled(int direction)
	{
		scrollDirection = direction;
		return true;
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
	public boolean touchUp(int screenX, int screenY, int pointer, int button)
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

	private boolean zoomIn()
	{
		scrollDirection = SCROLL_NONE;
		if (camera.position.dst(ORIGIN) > MIN_DIST)
		{
			camera.position.scl(ZOOM_RATE);
			return true;
		}
		return false;
	}

	private boolean zoomOut()
	{
		scrollDirection = SCROLL_NONE;
		if (camera.position.dst(ORIGIN) < MAX_DIST)
		{
			camera.position.scl(1 / ZOOM_RATE);
			return true;
		}
		return false;
	}
}
