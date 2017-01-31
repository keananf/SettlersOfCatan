package catan;

import board.Edge;
import board.Node;
import client.ClientGame;
import enums.Colour;
import game.InProgressTurn;
import protocol.BoardProtos.NodeProto;
import protocol.BuildProtos.*;
import protocol.BuildProtos.RoadProto;
import protocol.RequestProtos.Request;

public class ClientWorker
{
	private ClientGame game;
	private InProgressTurn inProgressTurn = game.inProgressTurn;

	public ClientWorker(ClientGame game)
	{
		this.game = game;
	}

	public void update()
	{
		
		if(inProgressTurn.chosenMove != null)
		{
			sendMove();
		}
		else if(inProgressTurn.initialClickObject == inProgressTurn.initialClickObject.CARD)
		{
			checkCard();
		}
		else if(inProgressTurn.initialClickObject == inProgressTurn.initialClickObject.NODE)
		{
			checkBuild(inProgressTurn.chosenNode);
		}
		else if(inProgressTurn.initialClickObject == inProgressTurn.initialClickObject.EDGE)
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
		Request.Builder request = Request.newBuilder();

		switch(game.inProgressTurn.chosenMove){
			case BUILD_ROAD:
				request.setBuildRoadRequest(setUpRoad(inProgressTurn.chosenEdge,col));
				break;
            case BUILD_SETTLEMENT:
				request.setBuildSettlementRequest(setUpSettlement(x1 ,y1, type, col));//type settlement
				break;
            case UPGRADE_SETTLEMENT:
                request.setupradeSettlementRequest();// becomes city
                break;
            case BUY_CARD:
                request.buyDevCardRequest();
                break;
            case CLOSE:
                request.endMoveRequest();
                break;
		}

		//TODO: create protocol buffer from game state
		//TODO: send protocol buffer to server
	}

	private RoadProto setUpRoad(Edge edge, Colour col)
    {
        RoadProto.Builder road = RoadProto.newBuilder();
		PointProto.Builder node = PointProto.newBuilder();


        //Node1
        node.setX(edge.getX().getX());
        node.setY(edge.getX().getY());
        road.setP1(node.build());

        //Node2
        node.setX(x2);
        node.setY(y2);
        road.setP2(node.build());

        road.setPlayerId(col);

        return road.build();
    }
}
