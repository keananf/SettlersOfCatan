package grid;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

public abstract class GridElement implements BoardElement
{
	private int x, y; // coordinates

	GridElement(int x, int y)
	{
		setX(x);
		setY(y);
	}

	/**
	 * @return the x
	 */
	public int getX()
	{
		return x;
	}

	/**
	 * @param x the x to set
	 */
	private void setX(int x)
	{
		this.x = x;
	}

	/**
	 * @return the y
	 */
	public int getY()
	{
		return y;
	}

	/**
	 * @param y the y to set
	 */
	private void setY(int y)
	{
		this.y = y;
	}

	public Vector2 get2DPos()
	{
		return new Vector2((float) x, (float) ((2 * (float) y - (float) x) / Math.sqrt(3)));
	}

	public Vector3 get3DPos()
	{
		return new Vector3(get2DPos().x, 0, get2DPos().y);
	}
}
