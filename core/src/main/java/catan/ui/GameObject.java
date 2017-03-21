package catan.ui;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import java.awt.Point;
public class GameObject extends ModelInstance
{
	protected final Vector3 centre;
	protected final Point catanCoord;
	
	
	public GameObject(final Model model, final Point catanCoord)
	{
		super(model, new Vector3((float)catanCoord.getX(), 0f, (float) ((2 * (float)catanCoord.getY() - (float)catanCoord.getX()) / Math.sqrt(3))));
		this.centre = new Vector3((float)catanCoord.getX(), 0f, (float) ((2 * (float)catanCoord.getY() - (float)catanCoord.getX()) / Math.sqrt(3)));
		this.catanCoord = catanCoord;
	}
}
