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

class TurnProcessor
{
	private final Client client;
	private IServerConnection conn;

	public TurnProcessor(IServerConnection conn, Client client)
	{
		this.client = client;
		this.conn = conn;
	}

	/**
	 * Switches on the move type to ascertain which proto message to form
	 */
	void setUpMove()
	{
		Requests.Request.Builder request = Requests.Request.newBuilder();

		switch (getTurn().getChosenMove())
		{
		case BUILDROAD:
			request.setBuildRoad(getTurn().getChosenEdge().toEdgeProto());
			break;
		case BUILDSETTLEMENT:
			request.setBuildSettlement(getTurn().getChosenNode().toProto());
			break;
		case BUILDCITY:
			request.setBuildCity(getTurn().getChosenNode().toProto());
			break;
		case CHATMESSAGE:
			request.setChatMessage(getTurn().getChatMessage());
			break;
		case JOINLOBBY:
			request.setJoinLobby(getJoinLobby());
			break;
		case MOVEROBBER:
			request.setMoveRobber(getTurn().getChosenHex().toHexProto().getLocation());
			break;
		case INITIATETRADE:
			request.setInitiateTrade(getTrade());
			break;
		case CHOOSERESOURCE:
			request.setChooseResource(ResourceType.toProto(getTurn().getChosenResource()));
			break;
		case DISCARDRESOURCES:
			request.setDiscardResources(getGame().processResources(getTurn().getChosenResources()));
			break;
		case SUBMITTRADERESPONSE:
			request.setSubmitTradeResponse(getTurn().getTradeResponse());
			break;
		case PLAYDEVCARD:
			request.setPlayDevCard(DevelopmentCardType.toProto(getTurn().getChosenCard()).getPlayableDevCard());
			break;
		case SUBMITTARGETPLAYER:
			request.setSubmitTargetPlayer(
					Board.Player.newBuilder().setId(getGame().getPlayer(getTurn().getTarget()).getId()).build());
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
		case GETRESOURCES:
			request.setGetResources(EmptyOuterClass.Empty.getDefaultInstance());
			break;
		case BODY_NOT_SET:
		default:
			return;
		}

		sendToServer(request.build());
	}

	/**
	 * Sends the given request to the server
	 * 
	 * @param request the request to send
	 */
	private void sendToServer(Requests.Request request)
	{
		try
		{
			conn.sendMessageToServer(Messages.Message.newBuilder().setRequest(request).build());
		}
		catch (Exception e)
		{
			conn = null;
			client.log("Client Error",
					String.format("Error sending request %s to server", request.getBodyCase().name()));
			client.shutDown();
			e.printStackTrace();
		}
	}

	/**
	 * @return a join lobby event for this player
	 */
	private Lobby.Join getJoinLobby()
	{
		// TODO update gameID?
		return Lobby.Join.newBuilder().setUsername(client.getPlayer().getUsername()).setGameId(0).build();
	}

	/**
	 * @return the trade event for this player
	 */
	private Trade.Kind getTrade()
	{
		Trade.Kind.Builder kind = Trade.Kind.newBuilder();

		// If a player trade
		if (getTurn().getPlayerTrade() != null)
		{
			kind.setPlayer(getTurn().getPlayerTrade()).build();
		}
		else if (getTurn().getBankTrade() != null)
		{
			kind.setBank(getTurn().getBankTrade()).build();
		}

		return kind.build();
	}

	private ClientGame getGame()
	{
		return client.getState();
	}

	private Turn getTurn()
	{
		return client.getTurn();
	}
}
