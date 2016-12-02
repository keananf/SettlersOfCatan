package main.java.comm;

import main.java.enums.MoveType;


/**
 * Class describing the wrapper over request messages
 * @author 140001596
 */
public class Request
{
	private MoveType type;
	private String msg; // move as json
	
	/**
	 * @return the type
	 */
	public MoveType getType()
	{
		return type;
	}
	/**
	 * @param type the type to set
	 */
	public void setType(MoveType type)
	{
		this.type = type;
	}
	/**
	 * @return the msg
	 */
	public String getMsg()
	{
		return msg;
	}
	/**
	 * @param msg the msg to set
	 */
	public void setMsg(String msg)
	{
		this.msg = msg;
	}
}