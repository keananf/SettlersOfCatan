package grid;

import com.badlogic.gdx.math.Vector3;

public abstract class GridElement implements BoardElement
{
	private int x, y; // coordinates
	protected GridElement(int x, int y)
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
	public void setX(int x)
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
	public void setY(int y)
	{
		this.y = y;
	}

	public Vector3 getCartesian(){
		return new Vector3((float)x, 0f, (float) ((2 * (float)y - (float)x) / Math.sqrt(3)));
		
		
		
	}
	
	
	
	
	
}
