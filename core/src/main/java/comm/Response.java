package main.java.comm;

import main.java.enums.MoveType;


/**
 * Class describing the wrapper over response messages
 * @author 140001596
 */
public class Response
{
	private String response;
	private MoveType type;
	private String msg; // move as json
	
	/**
	 * @return the status
	 */
	public String getResponse()
	{
		return response;
	}
	/**
	 * @param response the status to set
	 */
	public void setResponse(String response)
	{
		this.response = response;
	}
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
