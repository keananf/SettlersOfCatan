package main.java.game.players;

import java.net.InetAddress;

import main.java.enums.Colour;
import main.java.game.moves.*;

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

	@Override
	public Move receiveMove()
	{
		// TODO Auto-generated method stub
		return new BuildSettlementMove();
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