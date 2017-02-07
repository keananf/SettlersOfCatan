package catan;

import board.Edge;
import board.Node;
import client.ClientGame;
import enums.Move;
import enums.ResourceType;
import game.InProgressTurn;
import game.build.Building;
import game.build.Settlement;
import game.players.Player;
import protocol.BuildProtos.PointProto;
import protocol.RequestProtos.*;
import protocol.RequestProtos.BuildRoadRequest.Builder;
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
		inProgressTurn.possibilities[0] = Move.CLOSE;
		
		if(checkTurn())
		{
			Player player = game.getPlayer();
			
			Map<ResourceType, Integer> playerResources = player.getResources();
			
			int ore = playerResources.get(ResourceType.Ore);
			int grain = playerResources.get(ResourceType.Grain);
			int wool = playerResources.get(ResourceType.Wool);
			
			if(ore >= 1 && grain >= 1 && wool >= 1)
			{
				inProgressTurn.possibilities[1] = Move.BUY_CARD;
			}
		}
	}

	private void checkBuild(Node node)
	{
		inProgressTurn.possibilities[0] = Move.CLOSE;
		
		if(checkTurn())
		{
			Building building = node.getSettlement();
			
			if(building != null)
			{
				if(building instanceof Settlement)
				{
					if(building.getPlayerColour() == game.getPlayer().getColour())
					{
						Player player = game.getPlayer();
						
						Map<ResourceType, Integer> playerResources = player.getResources();
						
						int ore = playerResources.get(ResourceType.Ore);
						int grain = playerResources.get(ResourceType.Grain);
						
						if(ore >= 3 && grain >= 2)
						{
							inProgressTurn.possibilities[1] = Move.UPGRADE_SETTLEMENT;
						}

					}
				}
			}
			else
			{
				if(game.getPlayer().canBuildSettlement(node))
				{
					inProgressTurn.possibilities[1] = Move.BUILD_SETTLEMENT;
				}
			}
		}
	}

    private void checkBuild(Edge edge) {
        //TODO: exactly the same as above but for edges
    }

    private boolean checkTurn() {
        boolean turn = false;

        if (game.getCurrentPlayer() == game.getPlayer().getColour()) {
            turn = true;
        }

        return turn;
    }
	private void checkBuild(Edge edge)
	{
		inProgressTurn.possibilities[0] = Move.CLOSE;

		if(game.getPlayer().canBuildRoad(edge))
		{
			inProgressTurn.possibilities[1] = Move.BUILD_ROAD;
		}
	}

	private boolean checkTurn()
	{
		boolean turn = false;

		if(game.getCurrentPlayer() == game.getPlayer().getColour())
		{
			turn = true;
		}

		return turn;
	}

    private Request sendMove() {
        Request.Builder request = Request.newBuilder();

        switch (game.inProgressTurn.chosenMove) {
            case BUILD_ROAD:
                request.setBuildRoadRequest(setUpRoad(inProgressTurn.chosenEdge));
                break;
            case BUILD_SETTLEMENT:
                request.setBuildSettlementRequest(setUpSettlement(inProgressTurn.chosenNode));//just Settlement
                break;
            case UPGRADE_SETTLEMENT:
                request.setUpradeSettlementRequest(upgradeSettlement(inProgressTurn.chosenNode));// becomes city
                break;
            case BUY_CARD:
                request.setBuyDevCardRequest(buyDevCardRequest());
                break;
            case CLOSE:
                request.setEndMoveRequest(endMoveRequest());
                break;
        }

        return request.build();
    }

    //TODO: send protocol buffer to server

    private BuildRoadRequest setUpRoad(Edge edge) {
        BuildRoadRequest.Builder roadRequest = BuildRoadRequest.newBuilder();//TODO: add affordability check, unless done before adding to chosen move
        roadRequest.setEdge(edge.toEdgeProto());

        return roadRequest.build();
    }

    private BuildSettlementRequest setUpSettlement(Node node) {
        BuildSettlementRequest.Builder settlementRequest = BuildSettlementRequest.newBuilder();//TODO: add affordability check
        PointProto.Builder point = PointProto.newBuilder();

        point.setX(node.getX());
        point.setY(node.getY());

        settlementRequest.setPoint(point.build());
        return settlementRequest.build();


    }

    private UpgradeSettlementRequest upgradeSettlement(Node node) {
        if (node.getSettlement() == null) return null;//TODO: add affordability check

        UpgradeSettlementRequest.Builder upgradeRequest = UpgradeSettlementRequest.newBuilder();
        PointProto.Builder point = PointProto.newBuilder();

        point.setX(node.getX());
        point.setY(node.getY());

        upgradeRequest.setPoint(point.build());
        return upgradeRequest.build();

    }

    private BuyDevCardRequest buyDevCardRequest() {
        BuyDevCardRequest.Builder buyDevCard = BuyDevCardRequest.newBuilder();
        return buyDevCard.build();
    }

    private EndMoveRequest endMoveRequest() {
        EndMoveRequest.Builder endMove = EndMoveRequest.newBuilder();
        return endMove.build();
    }
}
