package catan.ui;

import java.util.concurrent.Semaphore;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import grid.Node;
import intergroup.Requests;
import client.Client;
import game.build.Building;
import game.build.Settlement;
import grid.BoardElement;
import grid.Edge;
import grid.Hex;

public class MoveBuilder {
private final Client client;
	public MoveBuilder(Client client)
	{
		this.client = client;	
	}
	
	protected void onSelect(BoardElement element) {

		if (element instanceof Node) {
			client.getTurn().setChosenNode((Node) element);
			Building building = ((Node) element).getSettlement();
			if (building == null) {
				client.getTurn().setChosenMove(Requests.Request.BodyCase.BUILDSETTLEMENT);
				
			} else if (building instanceof Settlement) {
				client.getTurn().setChosenMove(Requests.Request.BodyCase.BUILDCITY);
				
			} else {
				return;
			}

		}
		if (element instanceof Edge) {
			client.getTurn().setChosenEdge((Edge) element);
			if (((Edge) element).getRoad() == null) {
				client.getTurn().setChosenMove(Requests.Request.BodyCase.BUILDROAD);

			}
		} else {
			return;
		}

		if (element instanceof Hex) {
			client.getTurn().setChosenHex((Hex) element);
			client.getTurn().setChosenMove(Requests.Request.BodyCase.MOVEROBBER);
		}
		
		client.acquireLocksAndSendTurn(client.getTurn());
	}
}
