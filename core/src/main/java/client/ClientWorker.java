package client;

import enums.ClickObject;
import enums.DevelopmentCardType;
import game.build.Building;
import game.build.Settlement;
import game.players.Player;
import grid.Edge;
import grid.Node;
import intergroup.EmptyOuterClass;
import intergroup.Requests;
import intergroup.lobby.Lobby;

import java.io.IOException;
import java.net.Socket;

public class ClientWorker
{
	private ClientGame game;
	private InProgressTurn inProgressTurn;
	private Socket clientSocket;
	private static final int PORT = 12345;

	public ClientWorker(ClientGame game, String host) throws IOException
	{
		inProgressTurn = new InProgressTurn();
		this.game = game;
		clientSocket = new Socket(host, PORT);
	}

	/**
	 * Updates the turn in progress
	 */
	public void update()
	{
		ClickObject obj = inProgressTurn.getInitialClickObject();

		if(inProgressTurn.getChosenMove() != null)
		{
			sendMove();
			return;
		}

		// Perform action based upon what type of object was last selected
		switch(obj)
		{
			case BUYDEVCARD:
				checkBuyDevCard();
				break;
			case PLAYDEVCARD:
				checkPlayDevCard(inProgressTurn.getChosenCard());
				break;
			case EDGE:
				checkBuildRoad(inProgressTurn.getChosenEdge());
				break;
			case NODE:
				checkBuild(inProgressTurn.getChosenNode());
				break;
			case TRADEREQUEST:
				//TODO
				break;

		}
	}

	/**
	 * Checks the player can play the given card
	 * @param type the type of card wanting to be played
	 */
	private void checkPlayDevCard(DevelopmentCardType type)
	{
		// If player's turn
		if(checkTurn())
		{
			Player player = game.getPlayer();

			// If the player owns the provided card
			if(player.getDevelopmentCards().containsKey(type) && player.getDevelopmentCards().get(type) > 0)
			{
				inProgressTurn.addPossibility(Requests.Request.BodyCase.PLAYDEVCARD);
			}
			else inProgressTurn.getPossibilities().remove(Requests.Request.BodyCase.PLAYDEVCARD);
		}
		else inProgressTurn.getPossibilities().remove(Requests.Request.BodyCase.PLAYDEVCARD);
	}

	/**
	 * Checks to see if the player can buy a dev card
	 */
	private void checkBuyDevCard()
	{
		// If player's turn
		if(checkTurn())
		{
			Player player = game.getPlayer();

			if(player.canAfford(DevelopmentCardType.getCardCost()))
			{
				inProgressTurn.addPossibility(Requests.Request.BodyCase.BUYDEVCARD);
			}
		}
	}

	/**
	 * Checks if a city or settlement can be built on the given nodeS
	 * @param node the desired settlement / city location
	 */
	private void checkBuild(Node node)
	{
		// If player's turn
		if(checkTurn())
		{
			Building building = node.getSettlement();

			// If there is a settlement present
			if(building != null && building instanceof Settlement)
			{
				if(game.getPlayer().canBuildCity(node))
				{
					inProgressTurn.addPossibility(Requests.Request.BodyCase.BUILDCITY);
				}
			}
			else
			{
				if(game.getPlayer().canBuildSettlement(node))
				{
					inProgressTurn.addPossibility(Requests.Request.BodyCase.BUILDSETTLEMENT);
				}
			}
		}
	}

	/**
	 * @return whether or not it is the player's turn
	 */
    private boolean checkTurn()
	{
        boolean turn = false;

        if (game.getCurrentPlayer() == game.getPlayer().getColour())
        {
            turn = true;
        }

        return turn;
    }

	/**
	 * Checks to see if the player can build a road
	 * @param edge the desired road location
	 */
	private void checkBuildRoad(Edge edge)
	{
		if(game.getPlayer().canBuildRoad(edge))
		{
			inProgressTurn.addPossibility(Requests.Request.BodyCase.BUILDROAD);
		}
	}

	/**
	 * Switches on the move type to ascertain which proto message to form
	 */
    private void sendMove()
	{
        Requests.Request.Builder request = Requests.Request.newBuilder();

        switch (inProgressTurn.getChosenMove())
		{
			case BUILDROAD:
                request.setBuildRoad(inProgressTurn.getChosenEdge().toEdgeProto());
                break;
            case BUILDSETTLEMENT:
                request.setBuildSettlement(inProgressTurn.getChosenNode().toProto());
                break;
			case BUILDCITY:
                request.setBuildCity(inProgressTurn.getChosenNode().toProto());
                break;
			case CHATMESSAGE:
				request.setChatMessage(inProgressTurn.getChatMessage());
				break;
			case JOINLOBBY:
				request.setJoinLobby(getJoinLobby());
				break;
			case MOVEROBBER:
				request.setMoveRobber(inProgressTurn.getChosenHex().toHexProto().getLocation());
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

	//TODO: send protocol buffer to server with codedOutputStream
	private void sendToServer(Requests.Request request)
	{
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

}
