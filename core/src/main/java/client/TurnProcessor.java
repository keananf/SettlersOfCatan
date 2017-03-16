package client;

import enums.ResourceType;
import intergroup.EmptyOuterClass;
import intergroup.Requests;
import intergroup.board.Board;
import intergroup.lobby.Lobby;
import intergroup.trade.Trade;

import java.io.IOException;
import java.net.Socket;

public class TurnProcessor
{
	private ClientGame game;
	private TurnInProgress turn;
	private Socket clientSocket;

	public TurnProcessor(Socket clientSocket, ClientGame game)
	{
		turn = new TurnInProgress();
		this.game = game;
		this.clientSocket = clientSocket;
	}

	/**
	 * Switches on the move type to ascertain which proto message to form
	 */
    private void sendMove()
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
				request.setDiscardResources(game.processResources(turn.getChosenResources()));
				break;
			case SUBMITTRADERESPONSE:
				request.setSubmitTradeResponse(turn.getTradeResponse());
				break;
			case PLAYDEVCARD:
				// TODO
				break;
			case SUBMITTARGETPLAYER:
				request.setSubmitTargetPlayer(Board.Player.newBuilder().setId(game.getPlayer(turn.getTarget()).getId()).build());
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

		sendToServer(request.build());
    }

	/**
	 * Sends the given request to the server
	 * @param request the request to send
	 */
	private void sendToServer(Requests.Request request)
	{
		// TODO: send protocol buffer to server with codedOutputStream
		try
		{
			request.writeTo(clientSocket.getOutputStream());
			clientSocket.getOutputStream().flush();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			e.getMessage();
		}
	}

	/**
	 * @return a join lobby event for this player
	 */
	private Lobby.Join getJoinLobby()
	{
		// TODO update gameID?
		return Lobby.Join.newBuilder().setUsername(game.getPlayer().getUsername()).setGameId(0).build();
	}

	/**
	 *@return the trade event for this player
	 */
	private Trade.Kind getTrade()
	{
		Trade.WithBank.Builder bank = Trade.WithBank.newBuilder();
		Trade.WithPlayer.Builder player = Trade.WithPlayer.newBuilder();
		Trade.Kind.Builder kind = Trade.Kind.newBuilder();

		// If a player trade
		if(turn.getPlayerTrade() != null)
		{
			PlayerTrade p = turn.getPlayerTrade();

			// Set up player trade
			player.setOther(Board.Player.newBuilder().setId(game.getPlayer(p.getOther()).getId()).build());
			player.setWanting(game.processResources(p.getWanting()));
			player.setOffering(game.processResources(p.getOffer()));

			kind.setPlayer(player.build()).build();
		}
		else if(turn.getBankTrade() != null)
		{
			BankTrade b  = turn.getBankTrade();
			bank.setOffering(game.processResources(b.getOffer()));
			bank.setWanting((game.processResources(b.getWanting())));

			kind.setBank(bank.build()).build();
		}

		return kind.build();
	}

	public TurnInProgress getTurn()
	{
		return turn;
	}
}
