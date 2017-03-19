package catan.ui;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.ModelInstance;

class GameController implements InputProcessor
{
	private final Camera cam;
	private final Array<ModelInstance> instances;

	public GameController(Camera cam, Array<ModelInstance> instances)
	{
		this.cam = cam;
		this.instances = instances;
	}

	// https://xoppa.github.io/blog/interacting-with-3d-objects/
	public int getObject(int screenX, int screenY)
	{
		int result = -1;
		float distance = -1;

		return result;
	}

	@Override public boolean keyUp(int keycode) { return false; }
	@Override public boolean keyDown(int keycode) { return false; }
	@Override public boolean keyTyped(char character) { return false; }
	@Override public boolean touchDown(int screenX, int screenY, int pointer, int button) { return false; }
	@Override public boolean touchUp(int screenX, int screenY, int pointer, int button) { return false; }
	@Override public boolean touchDragged(int screenX, int screenY, int pointer) { return false; }
	@Override public boolean mouseMoved(int screenX, int screenY) { return false; }
	@Override public boolean scrolled(int amount) { return false; }
}
