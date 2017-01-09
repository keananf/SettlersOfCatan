package game;

import enums.*;
import exceptions.*;
import game.build.*;
import game.players.*;

import java.net.InetAddress;
import java.util.*;

import board.*;
import protocol.EnumProtos.*;
import protocol.RequestProtos.*;
import protocol.ResourceProtos.*;
import protocol.BoardProtos.*;
import protocol.ResponseProtos.*;
import protocol.BuildProtos.*;
import protocol.TradeProtos.*;

public class Game
{
	private HexGrid grid;
	private Map<Colour, Player> players;
	Random dice;
	private Player currentPlayer;
	private int current; // index of current player
	private Player playerWithLongestRoad;
	private int longestRoad;
	private int numPlayers;
	public static final int NUM_PLAYERS = 4;
	
	public Game()
	{
		grid = new HexGrid();
		players = new HashMap<Colour, Player>();
		dice = new Random();
	}

	/**
	 * Chooses first player.
	 */
	public void chooseFirstPlayer()
	{
		int dice = this.dice.nextInt(NUM_PLAYERS);
		
		current = dice;
		setCurrentPlayer(getPlayersAsList()[dice]);
	}

	/**
	 * Assigns resources to each player based upon their settlements and the dice
	 * @param dice the dice roll
	 * @return 
	 */
	public Map<Colour, Map<ResourceType, Integer>> allocateResources(int dice)
	{
		int resourceLimit = 7;
		Map<Colour, Map<ResourceType, Integer>> playerResources = new HashMap<Colour, Map<ResourceType, Integer>>();
		
		// for each player
		for(Player player : getPlayersAsList())
		{
			Map<ResourceType, Integer> grant = new HashMap<ResourceType, Integer>();
			
			// If 7, check that no one is above the resource limit
			if(dice == resourceLimit)
			{
				grant.putAll(player.loseResources());
                playerResources.put(player.getColour(), grant);
				continue;
			}
			
			// for each of this player's settlements
			for(Building building : player.getSettlements().values())
			{
				int amount = building instanceof City ? 2 : 1;
				List<Hex> hexes = ((Building)building).getNode().getHexes();
				
				// for each hex on this settlement
				for(Hex hex : hexes)
				{
					// If the hex's chit is equal to the dice roll
					if(hex.getChit() == dice && !hex.hasRobber())
					{
						grant.put(hex.getResource(), amount);
					}
				}
			}
			player.grantResources(grant);
			playerResources.put(player.getColour(), grant);
		}
		
		return playerResources;
	}
	
	/**
	 * If trade was successful, exchange of resources occurs here
	 * @param trade the trade object detailing the trade
	 * @return the response status
	 */
	public SuccessFailResponse processTrade(TradeProto trade, Player offerer) throws IllegalTradeException
	{
        SuccessFailResponse.Builder resp = SuccessFailResponse.newBuilder();

        // Find the recipient and extract the trade's contents
        int type = trade.getContentsCase();
		ResourceCount.Builder offer = trade.getOffer();
		ResourceCount.Builder request = trade.getRequestAsMap();
		Player recipient = players.get(trade.getRecipient()), recipientCopy = recipient.copy();
		
		try
		{
			// Exchange resources
			offerer.spendResources(offer);
			recipient.grantResources(offer);
			
			recipient.spendResources(request);
			offerer.grantResources(request);
		}
		catch(Exception e)
		{
			// Reset recipient and throw exception. Offerer is reset in above method
			recipient.restoreCopy(recipientCopy, null);
					
			throw new IllegalTradeException(offerer.getColour(), recipient.getColour());
		}

        resp.setResult(ResultProto.SUCCESS);
        return resp.build();
	}



	/**
	 * Checks that the player can build a road at the desired location, and builds it.
	 * @param move
	 * @param playerColour the player's colour
	 * @return the response message to the client
	 * @throws CannotUpgradeException 
	 * @throws CannotAffordException 
	 */
	public SuccessFailResponse upgradeSettlement(UpgradeSettlementRequest move, Colour playerColour) throws CannotAffordException, CannotUpgradeException
	{
	    SuccessFailResponse.Builder resp = SuccessFailResponse.newBuilder();
        Player p = players.get(playerColour);
		Node n = grid.getNode(move.getPoint().getX(), move.getPoint().getY());
		
		// Try to upgrade settlement
		p.upgradeSettlement(n);

		resp.setResult(ResultProto.SUCCESS);
		return resp.build();
    }
	
	/**
	 * Checks that the player can build a settlement at the desired location, and builds it.
	 * @param request the request
     * @param playerColour the player's colour
	 * @return the response message to the client
	 * @throws IllegalPlacementException 
	 * @throws CannotAffordException 
	 * @throws SettlementExistsException 
	 */
	public SuccessFailResponse buildSettlement(BuildSettlementRequest request, Colour playerColour) throws CannotAffordException, IllegalPlacementException, SettlementExistsException
	{
	    SuccessFailResponse.Builder resp = SuccessFailResponse.newBuilder();
        Player p = players.get(playerColour);
        PointProto pointProto = request.getPoint();
		Node node = grid.getNode(pointProto.getX(), pointProto.getY());
		
		// Try to build settlement
		p.buildSettlement(node);
		
		// Check all combinations of edges to check if a road chain was broken
		for(int i = 0; i < node.getEdges().size(); i++)
		{
			boolean broken= false;
			for(int j = 0; j < node.getEdges().size(); j++)
			{
				Edge e = node.getEdges().get(i), other = node.getEdges().get(j);
				Road r = e.getRoad(), otherR = other.getRoad();
				
				if(e.equals(other)) continue;
				
				// If this settlement is between two roads of the same colour
				if(r != null && otherR != null && r.getPlayerColour().equals(otherR.getPlayerColour()))
				{
					// retrieve owner of roads and break the road chain
					players.get(e.getRoad().getPlayerColour()).breakRoad(e, other);
					broken = true;
					break;
				}
			}
			if(broken) break;
		}
		// TODO send out updated road counts

        resp.setResult(ResultProto.SUCCESS);
		return resp.build();
	}
	
	/**
	 * Checks that the player can buy a development card
	 * @param type the type of development card to play
	 * @param playerColour the player's colour
	 * @return the response message to the client
	 */
	public SuccessFailResponse playDevelopmentCard(DevelopmentCardType type, Colour playerColour)
	{
	    SuccessFailResponse.Builder resp = SuccessFailResponse.newBuilder();
	    Player p = players.get(playerColour);

		// Try to play card
		try
		{
			DevelopmentCard card = new DevelopmentCard();
			card.setType(type);
			card.setColour(playerColour);
			p.playDevelopmentCard(card);
            resp.setResult(ResultProto.SUCCESS);
		}
		catch (DoesNotOwnException e) 
		{
			// Error
            resp.setResult(ResultProto.FAILURE);
			resp.setReason(e.getMessage());
		}
		
		// Return success message
		return resp.build();
	}
	
	/**
	 * Checks that the player can buy a development card
	 * @param move the move
	 * @param playerColour the player's colour
	 * @return the response message to the client
	 * @throws CannotAffordException 
	 */
	public BuyDevCardResponse buyDevelopmentCard(BuyDevCardRequest move, Colour playerColour) throws CannotAffordException
	{
        BuyDevCardResponse.Builder resp = BuyDevCardResponse.newBuilder();
		Player p = players.get(playerColour);
		
		// Try to buy card
		// Return the response with the card parameter set
        DevelopmentCardType card = p.buyDevelopmentCard();
        resp.setDevelopmentCard(DevelopmentCardType.toProto(card));
        resp.setResult(ResultProto.SUCCESS);
		return resp.build();
	}
	
	/**
	 * Moves the robber and takes a card from the player
	 * who has a settlement on the hex
	 * @param move the move
     * @param playerColour the player's colour
	 * @return return message
	 * @throws CannotStealException if the specified player cannot provide a resource 
	 */
	public MoveRobberResponse moveRobber(MoveRobberRequest move, Colour playerColour) throws CannotStealException
	{
        MoveRobberResponse.Builder resp = MoveRobberResponse.newBuilder();

		// Retrieve the new hex the robber will move to.
        PointProto point = move.getHex().getP();
		Hex newHex = grid.getHex(point.getX(), point.getY());

		Colour otherColour = Colour.fromProto(move.getColourToTakeFrom());
		Player other = players.get(otherColour);
		boolean valid = false;

		// Verify this player can take from the specified one
		for(Node n : newHex.getNodes())
		{
			// If node has a settlement and it is of the specified colour, then the player can take a card
			// Randomly remove resource
			if(n.getSettlement() != null && n.getSettlement().getPlayerColour().equals(otherColour))
			{
                resp.setResource(ResourceType.toProto(currentPlayer.takeResource(other)));
				valid = true;
			}
		}
		
		// Cannot take from this player
		if(!valid) throw new CannotStealException(playerColour, otherColour);
		
		// return success message
		grid.swapRobbers(newHex);
		return resp.build();
	}

	/**
	 * Process the playing of the 'Build Roads' development card.
	 * @param request the request to process
     * @param playerColour the player's colour
	 * @return return message
	 * @throws RoadExistsException 
	 * @throws CannotBuildRoadException 
	 * @throws CannotAffordException 
	 */
	public PlayRoadBuildingCardResponse playBuildRoadsCard(PlayRoadBuildingCardRequest request, Colour playerColour) throws CannotAffordException, CannotBuildRoadException, RoadExistsException
	{
		PlayRoadBuildingCardResponse.Builder resp = PlayRoadBuildingCardResponse.newBuilder();

		// Set responses and return
		resp.setResponse1(buildRoad(request.getRequest1(), playerColour));
		resp.setResponse2(buildRoad(request.getRequest2(), playerColour));
		return resp.build();
	}
	
	/**
	 * Process the playing of the 'University' development card.
	 * @return return message
	 */
	public SuccessFailResponse playUniversityCard()
    {
        SuccessFailResponse.Builder resp = SuccessFailResponse.newBuilder();
        grantVpPoint();

        resp.setResult(ResultProto.SUCCESS);
        return resp.build();
	}
	
	/**
	 * Process the playing of the 'Library' development card.
	 * @return return message
	 */
	public SuccessFailResponse playLibraryCard()
	{
        SuccessFailResponse.Builder resp = SuccessFailResponse.newBuilder();
        grantVpPoint();

        resp.setResult(ResultProto.SUCCESS);
        return resp.build();
	}
	
	/**
	 * Process the playing of the 'Year of Plenty' development card.
	 * @param move the move to process
	 * @return return message
	 */
	public SuccessFailResponse playYearOfPlentyCard(PlayYearOfPlentyCardRequest move)
	{
	    SuccessFailResponse.Builder resp = SuccessFailResponse.newBuilder();

        // Set up grant
		Map<ResourceType, Integer> grant = new HashMap<ResourceType, Integer>();
		grant.put(ResourceType.fromProto(move.getR1()), 1);
		grant.put(ResourceType.fromProto(move.getR2()), 1);
		currentPlayer.grantResources(grant);
		
		// In receiving success, the client knows the request has been granted
        resp.setResult(ResultProto.SUCCESS);
		return resp.build();
	}
	
	/**
	 * Process the playing of the 'Monopoly' development card.
	 * @param request the request to process
	 * @return return message
	 */
	public PlayMonopolyCardResponse playMonopolyCard(PlayMonopolyCardRequest request)
	{
        PlayMonopolyCardResponse.Builder response = PlayMonopolyCardResponse.newBuilder();
		ResourceTypeProto r = request.getResource();
		Map<ResourceType, Integer> grant = new HashMap<ResourceType, Integer>();
		int sum = 0;
		
		// for each player
		for(Player p : players.values())
		{
			if(p.equals(currentPlayer)) continue;
			
			try
			{
				// Give p's resources of type 'r' to currentPlayer
				int num = p.getResources().get(ResourceType.fromProto(r));
				grant.put(ResourceType.fromProto(r), num);
				p.spendResources(grant);
				sum += num;
			} 
			catch (CannotAffordException e) { /* Will never happen */ }
			currentPlayer.grantResources(grant);
		}
		
		// Return message is string showing number of resources taken
        response.setNumResources(sum);
		return response.build();
	}
	
	/**
	 * Checks that the player can build a road at the desired location, and builds it.
	 * @param request
	 * @return the response message to the client
	 * @throws RoadExistsException 
	 * @throws CannotBuildRoadException 
	 * @throws CannotAffordException 
	 */
	public BuildRoadResponse buildRoad(BuildRoadRequest request, Colour colour) throws CannotAffordException, CannotBuildRoadException, RoadExistsException
	{
		Player p = players.get(colour);
		PointProto p1 = request.getEdge().getP1(), p2 = request.getEdge().getP2();
		Node n = grid.getNode(p1.getX(), p1.getY());
		Node n2 = grid.getNode(p2.getX(), p2.getY());
		Edge edge  = null;
        BuildRoadResponse.Builder response = BuildRoadResponse.newBuilder();
		
		// Find edge
		for(Edge e : n.getEdges())
		{
			if(e.getX().equals(n2) || e.getY().equals(n2))
			{
				edge = e;
				break;
			}
		}
		
		// Try to build the road and update the longest road 
		int longestRoad = p.buildRoad(edge);
		checkLongestRoad();
		
		// return success message
        response.setLongestRoad(longestRoad);
		return response.build();
	}
	
	/**
	 * Checks who has the longest road
	 */
	private void checkLongestRoad()
	{
		Player current = getCurrentPlayer();
		
		// Calculate who has longest road
		int length = current.calcRoadLength();
		if(length > longestRoad)
		{
			// Update victory points
			if(longestRoad >= 5)
			{
				playerWithLongestRoad.setVp(playerWithLongestRoad.getVp() - 2);
			}
			if (length >= 5) current.setVp(current.getVp() + 2);
			if(playerWithLongestRoad != null) playerWithLongestRoad.setHasLongestRoad(false);
			
			longestRoad = length;
			playerWithLongestRoad = getCurrentPlayer();
			current.setHasLongestRoad(true);
		}
	}

	/**
	 * Toggles a player's turn
	 * @return 
	 */
	public EndMoveResponse changeTurn()
	{
		EndMoveResponse.Builder resp = EndMoveResponse.newBuilder();

		setCurrentPlayer(getPlayersAsList()[++current % NUM_PLAYERS]);
		resp.setNewTurn(Colour.toProto(currentPlayer.getColour()));

		return resp.build();
	}

	/**
	 * Generates a random roll between 1 and 12
	 */
	public int generateDiceRoll()
	{
		return dice.nextInt(12) + 1;
	}

	/**
	 * Looks to see if any player has won
	 * @return true if a player has won
	 */
	public boolean isOver()
	{
		for(Player p : getPlayersAsList())
		{
			if(p.hasWon()) return true;
		}
		
		return false;
	}

	/**
	 * @return the grid
	 */
	public HexGrid getGrid()
	{
		return grid;
	}

	public Map<Colour, Player> getPlayers()
	{
		return players;
	}
	
	public Player[] getPlayersAsList()
	{
		return players.values().toArray(new Player[]{});
	}

	public Colour addNetworkPlayer(InetAddress inetAddress)
	{
	    Colour newCol = Colour.values()[numPlayers++];
		NetworkPlayer p = new NetworkPlayer(newCol);
		p.setInetAddress(inetAddress);
		
		players.put(p.getColour(), p);
		
		return p.getColour();
	}

	public void addPlayer(Player p)
	{
		players.put(p.getColour(), p);
	}

	/**
	 * @return the currentPlayer
	 */
	public Player getCurrentPlayer()
	{
		return currentPlayer;
	}

	/**
	 * @param currentPlayer the currentPlayer to set
	 */
	public void setCurrentPlayer(Player currentPlayer)
	{
		this.currentPlayer = currentPlayer;
	}

	private void grantVpPoint()
	{
		currentPlayer.setVp(currentPlayer.getVp() + 1);
	}

	/**
	 * @return a representation of the board that is compatible with protofbufs
	 */
    public GiveBoardResponse getBoard()
    {
    	GiveBoardResponse.Builder resp = GiveBoardResponse.newBuilder();
        BoardProto.Builder builder = BoardProto.newBuilder();
        int index = 0;

        // Add edges
        for(Edge e : getGrid().getEdgesAsList())
        {
            builder.setEdges(index++, e.toEdgeProto());
        }

        // Add hexes
        index = 0;
        for(Hex h : getGrid().getHexesAsList())
        {
            builder.setHexes(index++, h.toHexProto());
        }

        // Add port
        index = 0;
        for(Port p : getGrid().getPortsAsList())
        {
            builder.setPorts(index++, p.toPortProto());
        }


        // Add nodes
        index = 0;
        for(Node n : getGrid().getNodesAsList())
        {
            builder.setNodes(index++, n.toProto());
        }

        resp.setBoard(builder.build());
        return resp.build();
    }

	public void restorePlayerFromCopy(Player copy, DevelopmentCard card)
	{
		players.get(copy.getColour()).restoreCopy(copy, card);
	}
}
