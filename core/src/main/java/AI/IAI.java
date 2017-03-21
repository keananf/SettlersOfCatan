package AI;

import client.Turn;

import java.util.List;

public interface IAI 
{
	int rankMove(Turn turn);
	Turn selectMove(List<Turn> optimalMoves);
	void performMove();
}
