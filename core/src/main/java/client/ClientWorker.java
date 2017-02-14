package client;

import board.Edge;
import board.Hex;
import board.Node;
import enums.Colour;
import enums.Move;
import enums.ResourceType;
import game.build.Building;
import game.build.Settlement;
import game.players.Player;
import protocol.BuildProtos.PointProto;
import protocol.RequestProtos.*;
import protocol.TradeProtos.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Map;

public class ClientWorker
{
	private ClientGame game;
	private InProgressTurn inProgressTurn = game.inProgressTurn;
	private Socket clientSocket;
	private static final int PORT = 12345;

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
		else if(inProgressTurn.initialClickObject == inProgressTurn.initialClickObject.DEVCARD)
		{
			checkDevCard();
		}
		else if(inProgressTurn.initialClickObject == inProgressTurn.initialClickObject.TRADEREQUEST){
			//TODO: implement player trade and bank trade
		}

	}

	private void checkDevCard()
	{
		//TODO: look up game rules and validate
		inProgressTurn.possibilities[0] = Move.CLOSE;

		if(checkTurn()){
			Player player = game.getPlayer();
			// switch to see if card typ being played belongs to  player and set possibilities 1
			// -->
			//inProgressTurn.possibilities[1] = Move.PLAY...
			//make sure colour isn't your own and is present around hex
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

    private void sendMove() {
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
			case PLAY_ROADBUILDINGCARD:
			    request.setPlayRoadBuildingCardRequest(playRoadBuildCard(inProgressTurn.chosenEdges));
				break;
			case PLAY_MONOPOLYCARD:
				request.setPlayMonopolyCardRequest(playMonopolyCard(inProgressTurn.chosenResource));
				break;
			case PLAY_YEAROFPLENTYCARD:
				request.setPlayYearOfPlentyCardRequest(playYearOfPlentyCard(inProgressTurn.chosenResources));
				break;
			case PLAY_ROADLIBRARYCARD:
				request.setPlayLibraryCardRequest(playLibraryCard());
				break;
			case PLAY_UNIVERSITYCARD:
				request.setPlayUniversityCardRequest(playUniversityCard());
				break;
			case PLAY_KNIGHTCARD:
				request.setPlayKnightCardRequest(playKnightCard(inProgressTurn.chosenHex, inProgressTurn.chosenColour));
				break;
			case TRADE_REQUEST:
				request.setTradeRequest(tradeRequest());
				break;
            case CLOSE:
                request.setEndMoveRequest(endMoveRequest());
                break;


        }

		toServer(request.build());
    }




	//TODO: send protocol buffer to server with codedOutputStream
	private void toServer(Request request){
		try {
			clientSocket = new Socket(InetAddress.getLocalHost(),PORT);
			System.out.println("Sending to Server\n");

			request.writeTo(clientSocket.getOutputStream());
			clientSocket.getOutputStream().flush();

		} catch (IOException e) {
			e.printStackTrace();
			e.getMessage();
		}
	}



	private PlayKnightCardRequest playKnightCard(Hex hex, Colour colour) {
		if(hex == null || colour == null) return null;

		PlayKnightCardRequest.Builder knightCardRequest = PlayKnightCardRequest.newBuilder();
		MoveRobberRequest.Builder moveRobber = MoveRobberRequest.newBuilder();

		moveRobber.setColourToTakeFrom(Colour.toProto(colour));
		moveRobber.setHex(hex.toHexProto());

		knightCardRequest.setRequest(moveRobber.build());
		return knightCardRequest.build();
	}

	private TradeRequest tradeRequest() {
		return TradeRequest.newBuilder().build();
	}

	private PlayUniversityCardRequest playUniversityCard() {
		return PlayUniversityCardRequest.newBuilder().build();
	}

	private PlayLibraryCardRequest playLibraryCard() {
		return PlayLibraryCardRequest.newBuilder().build();
	}

	private PlayYearOfPlentyCardRequest playYearOfPlentyCard(ResourceType[] resources){
		if(resources==null || resources[0]==null || resources[1]==null) return null;

		PlayYearOfPlentyCardRequest.Builder yearOfPlentyCardRequest = PlayYearOfPlentyCardRequest.newBuilder();

		yearOfPlentyCardRequest.setR1(ResourceType.toProto(resources[0]));
		yearOfPlentyCardRequest.setR2(ResourceType.toProto(resources[1]));

		return yearOfPlentyCardRequest.build();
	}

	private PlayRoadBuildingCardRequest playRoadBuildCard(Edge[] edges){

		if(edges==null || edges[0]==null || edges[1]==null) return null;

		PlayRoadBuildingCardRequest.Builder roadBuildingCardRequest = PlayRoadBuildingCardRequest.newBuilder();
		BuildRoadRequest.Builder buildRoadRequest = BuildRoadRequest.newBuilder();

		buildRoadRequest.setEdge(edges[0].toEdgeProto());
		roadBuildingCardRequest.setRequest1(buildRoadRequest.build());

		buildRoadRequest.setEdge(edges[1].toEdgeProto());
		roadBuildingCardRequest.setRequest2(buildRoadRequest.build());


		return roadBuildingCardRequest.build();
	}

	private PlayMonopolyCardRequest playMonopolyCard(ResourceType resource){
		if(resource==null) return null;

		PlayMonopolyCardRequest.Builder monopolyCardRequest = PlayMonopolyCardRequest.newBuilder();
		monopolyCardRequest.setResource(ResourceType.toProto(resource));

		return monopolyCardRequest.build();
	}

	private BuildRoadRequest setUpRoad(Edge edge) {
        if(edge==null) return null;

		BuildRoadRequest.Builder roadRequest = BuildRoadRequest.newBuilder();
        roadRequest.setEdge(edge.toEdgeProto());

        return roadRequest.build();
    }

    private BuildSettlementRequest setUpSettlement(Node node) {
		if(node == null) return null;

        BuildSettlementRequest.Builder settlementRequest = BuildSettlementRequest.newBuilder();
        PointProto.Builder point = PointProto.newBuilder();

        point.setX(node.getX());
        point.setY(node.getY());

        settlementRequest.setPoint(point.build());
        return settlementRequest.build();


    }

    private UpgradeSettlementRequest upgradeSettlement(Node node) {
        if (node.getSettlement() == null) return null;

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
