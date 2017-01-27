import board.Edge;
import board.Node;
import game.Game;
import game.InProgressTurn;
import enums.Move;
import enums.ClickObject;

public class ClientWorker
{
	private Game game;

	public ClientWorker(Game game)
	{
		this.game = game;
	}

	public void update()
	{
		InProgressTurn inProgressTurn = game.inProgressTurn;
		
		if(inProgressTurn.chosenMove != null)
		{
			sendMove();
		}
		else if(inProgressTurn.initialClickObject == ClickObject.CARD)
		{
			checkCard();
		}
		else if(inProgressTurn.initialClickObject == ClickObject.NODE)
		{
			checkBuild(inProgressTurn.chosenNode);
		}
		else if(inProgressTurn.initialClickObject == ClickObject.EDGE)
		{
			checkBuild(inProgressTurn.chosenEdge);
		}

	}

	private void checkCard()
	{
		//TODO: check that the player can buy a development card
	}

	private void checkBuild(Node node)
	{
		//TODO: check which build actions the player can take and update game state
	}

	private void checkBuild(Edge edge)
	{
		//TODO: exactly the same as above but for edges
	}

	private void sendMove()
	{
		//TODO: create protocol buffer from game state
		//TODO: send protocol buffer to server
	}
}
