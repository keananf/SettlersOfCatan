package catan.ui;

import grid.Node;
import intergroup.Requests;
import client.Client;
import client.Turn;
import game.build.Building;
import game.build.Settlement;
import grid.BoardElement;
import grid.Edge;
import grid.Hex;

class MoveBuilder
{
	private final Client client;

	MoveBuilder(Client client)
	{
		this.client = client;
	}

	protected void onSelect(BoardElement element)
	{
		Turn turn = new Turn();

		if (element instanceof Node)
		{
			turn.setChosenNode((Node) element);
			Building building = ((Node) element).getSettlement();
			if (building == null)
			{
				turn.setChosenMove(Requests.Request.BodyCase.BUILDSETTLEMENT);

			}
			else if (building instanceof Settlement)
			{
				turn.setChosenMove(Requests.Request.BodyCase.BUILDCITY);

			}
			else
			{
				return;
			}
		}

		if (element instanceof Edge)
		{
			turn.setChosenEdge((Edge) element);
			if (((Edge) element).getRoad() == null)
			{
				turn.setChosenMove(Requests.Request.BodyCase.BUILDROAD);

			}
			else
			{
				return;
			}
		}

		if (element instanceof Hex)
		{
			turn.setChosenHex((Hex) element);
			turn.setChosenMove(Requests.Request.BodyCase.MOVEROBBER);
		}

		client.acquireLocksAndSendTurn(turn);
	}
}