package game.players;

import game.enums.Colour;
import game.moves.BuildSettlementMove;
import game.moves.Move;

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