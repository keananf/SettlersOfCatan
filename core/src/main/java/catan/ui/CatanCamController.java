package catan.ui;

import java.util.HashSet;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;

public final class CatanCamController implements InputProcessor
{
	private static final Vector3 ORIGIN = new Vector3(0f, 0f, 0f);
	private static final Vector3 Y_AXIS = new Vector3(0f, 1f, 0f);
	private static final float SPIN_RATE = 4f;
	private static final float ZOOM_RATE = 0.9f;
	private static final float MAX_DIST = 20f;
	private static final float MIN_DIST = 5f;

	private final Camera camera;
	private final HashSet<Integer> heldKeys = new HashSet<Integer>();

	public CatanCamController(final Camera camera)
	{
		this.camera = camera;
	}

	public void update()
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
		if (heldKeys.contains(Keys.UP) && camera.position.dst(ORIGIN) > MIN_DIST)
		{
			camera.position.scl(ZOOM_RATE);
			changed = true;
		}
		if (heldKeys.contains(Keys.DOWN) && camera.position.dst(ORIGIN) < MAX_DIST)
		{
			camera.position.scl(1/ZOOM_RATE);
			changed = true;
		}

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

	@Override
	public boolean scrolled(int amount)
	{
		return false;
	}
}
