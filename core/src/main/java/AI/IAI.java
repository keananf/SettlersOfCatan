package AI;

import java.util.ArrayList;

import client.ClientGame;
import enums.Difficulty;
import game.players.AIPlayer;

public interface IAI 
{
	public MoveEntry selectMove(ArrayList<MoveEntry> moves);
}
