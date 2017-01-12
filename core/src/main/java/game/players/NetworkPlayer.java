package game.players;

import java.net.InetAddress;

import enums.Colour;

/**
 * Class representing a player from across the network
 */
public class NetworkPlayer extends Player
{
	private InetAddress inetAddress;

	public NetworkPlayer(Colour colour)
	{
		super(colour);
	}


	/**
	 * @return the inetAddress
	 */
	public InetAddress getInetAddress()
	{
		return inetAddress;
	}

	/**
	 * sets the inetaddress of this network player
	 * @param inetAddress
	 */
	public void setInetAddress(InetAddress inetAddress)
	{
		this.inetAddress = inetAddress;
	}

}