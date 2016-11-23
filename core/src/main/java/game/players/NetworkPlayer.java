package main.java.game.players;

import main.java.game.enums.Colour;
import main.java.game.moves.*;

/**
 * Class representing a player from across the network
 */
public class NetworkPlayer extends Player
{

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

}