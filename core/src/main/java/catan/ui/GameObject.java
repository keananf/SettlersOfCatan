package catan.ui;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;

public class GameObject extends ModelInstance
{
	protected final Vector3 centre;

	public GameObject(final Model model, final Vector3 pos)
	{
		super(model, pos);
		this.centre = pos;
	}
}
