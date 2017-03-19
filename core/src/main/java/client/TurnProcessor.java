package client;

import connection.IServerConnection;
import enums.DevelopmentCardType;
import enums.ResourceType;
import intergroup.EmptyOuterClass;
import intergroup.Messages;
import intergroup.Requests;
import intergroup.board.Board;
import intergroup.lobby.Lobby;
import intergroup.trade.Trade;

public class TurnProcessor
{
	private final Client client;
	private final TurnInProgress turn;
	private final IServerConnection conn;

	public TurnProcessor(IServerConnection conn, Client client)
	{
		turn = client.getTurn();
		this.client = client;
		this.conn = conn;
	}


	/**
	 * Switches on the move type to ascertain which proto message to form
	 */
	protected void sendMove()
	{
		Requests.Request.Builder request = Requests.Request.newBuilder();

		switch (turn.getChosenMove())
		{
			case BUILDROAD:
                request.setBuildRoad(turn.getChosenEdge().toEdgeProto());
                break;
            case BUILDSETTLEMENT:
                request.setBuildSettlement(turn.getChosenNode().toProto());
                break;
			case BUILDCITY:
                request.setBuildCity(turn.getChosenNode().toProto());
                break;
			case CHATMESSAGE:
				request.setChatMessage(turn.getChatMessage());
				break;
			case JOINLOBBY:
				request.setJoinLobby(getJoinLobby());
				break;
			case MOVEROBBER:
				request.setMoveRobber(turn.getChosenHex().toHexProto().getLocation());
				break;
			case INITIATETRADE:
				request.setInitiateTrade(getTrade());
				break;
			case CHOOSERESOURCE:
				request.setChooseResource(ResourceType.toProto(turn.getChosenResource()));
				break;
			case DISCARDRESOURCES:
				request.setDiscardResources(getGame().processResources(turn.getChosenResources()));
				break;
			case SUBMITTRADERESPONSE:
				request.setSubmitTradeResponse(turn.getTradeResponse());
				break;
			case PLAYDEVCARD:
				request.setPlayDevCard(DevelopmentCardType.toProto(turn.getChosenCard()).getPlayableDevCard());
				break;
			case SUBMITTARGETPLAYER:
				request.setSubmitTargetPlayer(Board.Player.newBuilder().setId(getGame().getPlayer(turn.getTarget()).getId()).build());
				break;

			// Require empty request bodies
			case ROLLDICE:
				request.setRollDice(EmptyOuterClass.Empty.getDefaultInstance());
				break;
			case ENDTURN:
				request.setEndTurn(EmptyOuterClass.Empty.getDefaultInstance());
				break;
			case BUYDEVCARD:
				request.setBuyDevCard(EmptyOuterClass.Empty.getDefaultInstance());
				break;
        }

        // Send to server if it is a valid move
		if(client.getMoveProcessor().validateMsg(request.build()))
		{
			sendToServer(request.build());
		}
		// TODO else display error?
    }

	/**
	 * Sends the given request to the server
	 * 
	 * @param request the request to send
	 */
	private void sendToServer(Requests.Request request)
	{
		conn.sendMessageToServer(Messages.Message.newBuilder().setRequest(request).build());
	}

	/**
	 * @return a join lobby event for this player
	 */
	private Lobby.Join getJoinLobby()
	{
		// TODO update gameID?
		return Lobby.Join.newBuilder().setUsername(getGame().getPlayer().getUsername()).setGameId(0).build();
	}

	/**
	 * @return the trade event for this player
	 */
	private Trade.Kind getTrade()
	{
		Trade.Kind.Builder kind = Trade.Kind.newBuilder();

		// If a player trade
		if (turn.getPlayerTrade() != null)
		{
			kind.setPlayer(turn.getPlayerTrade()).build();
		}
		else if (turn.getBankTrade() != null)
		{
			kind.setBank(turn.getBankTrade()).build();
		}

		return kind.build();
	}

	private ClientGame getGame()
	{
		return client.getState();
	}
}
