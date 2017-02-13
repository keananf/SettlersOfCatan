package catan.ui;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;

public final class SpinCamController implements InputProcessor
{
	private static final Vector3 ORIGIN = new Vector3(0f, 0f, 0f);
	private static final Vector3 Y_AXIS = new Vector3(0f, 1f, 0f);
	private static final float SPIN_RATE = 4f;
	private static final float ZOOM_RATE = 0.9f;

	private final Camera camera;
	private boolean leftHeld, rightHeld, upHeld, downHeld;

	public SpinCamController(final Camera camera)
	{
		this.camera = camera;
	}

	public void update() {
		if (leftHeld)
			camera.rotateAround(ORIGIN, Y_AXIS, -SPIN_RATE);
		if (rightHeld)
			camera.rotateAround(ORIGIN, Y_AXIS, SPIN_RATE);
		if (upHeld)
			camera.position.scl(ZOOM_RATE);
		if (downHeld)
			camera.position.scl(1/ZOOM_RATE);

		camera.update();
	}

	@Override
	public boolean keyDown(int keycode)
	{
		if (keycode == Input.Keys.LEFT)
			leftHeld = true;
		if (keycode == Input.Keys.RIGHT)
			rightHeld = true;
		if (keycode == Input.Keys.UP)
			upHeld = true;
		if (keycode == Input.Keys.DOWN)
			downHeld = true;

		return true;
	}

	@Override
	public boolean keyUp(int keycode)
	{
		if (keycode == Input.Keys.LEFT)
			leftHeld = false;
		if (keycode == Input.Keys.RIGHT)
			rightHeld = false;
		if (keycode == Input.Keys.UP)
			upHeld = false;
		if (keycode == Input.Keys.DOWN)
			downHeld = false;

		return true;
	}

	@Override
	public boolean keyTyped(char character) { return false; }
	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) { return false; }
	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) { return false; }
	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) { return false; }
	@Override
	public boolean mouseMoved(int screenX, int screenY) { return false; }
	@Override
	public boolean scrolled(int amount) { return false; }
}
