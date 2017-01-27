package server;

import board.*;
import enums.Colour;
import enums.DevelopmentCardType;
import enums.ResourceType;
import exceptions.*;
import game.GameState;
import game.build.Building;
import game.build.City;
import game.build.Road;
import game.players.NetworkPlayer;
import game.players.Player;
import game.InProgressTurn;
import protocol.BoardProtos;
import protocol.BuildProtos.PointProto;
import protocol.EnumProtos.ResourceTypeProto;
import protocol.EnumProtos.ResultProto;
import protocol.EnumProtos.TradeStatusProto;
import protocol.RequestProtos.*;
import protocol.ResourceProtos.ResourceCount;
import protocol.ResponseProtos.*;
import protocol.TradeProtos.BankTradeProto;
import protocol.TradeProtos.PlayerTradeProto;
import protocol.TradeProtos.PortTradeProto;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ServerGame extends GameState
{
	private Map<Colour, Player> players; // Protected so that a client does NOT have access to other player info
	private Random dice;
	private int current; // index of current player

	private Player playerWithLongestRoad;
	private int longestRoad;
	private int numPlayers;
	public InProgressTurn inProgressTurn;

	public static final int NUM_PLAYERS = 4;

	public ServerGame()
	{
		super();
		players = new HashMap<Colour, Player>();
		dice = new Random();
		inProgressTurn = new InProgressTurn();
	}

	/**
	 * Chooses first player.
	 */
	public void chooseFirstPlayer()
	{
		int dice = this.dice.nextInt(NUM_PLAYERS);
		
		current = dice;
		setCurrentPlayer(getPlayersAsList()[dice].getColour());
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
				List<Hex> hexes = building.getNode().getHexes();
				
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
	public TradeStatusProto processBankTrade(BankTradeProto trade) throws IllegalBankTradeException, CannotAffordException
	{
		int exchangeAmount = 4, offerSum = 0, reqSum = 0;

		// Extract the trade's contents
		Player recipient = players.get(Colour.fromProto(trade.getPlayer()));
		Map<ResourceType, Integer> request = processResources(trade.getRequestResources());
		Map<ResourceType, Integer> offer = processResources(trade.getOfferResources());

		// Check that the player can afford the offer
		if(!recipient.canAfford(offer))
		{
			throw new CannotAffordException(recipient.getResources(), offer);
		}

		// Check that requested trade is allowed
		for(ResourceType r : ResourceType.values())
		{
			// sum up total quantities of offer and request
			offerSum += offer.containsKey(r) ? offer.get(r) : 0;
			reqSum += request.containsKey(r) ? request.get(r) : 0;

			// If too little or too many resources for a trade on this port
			if(offer.containsKey(r) && offer.get(r) % exchangeAmount != 0)
				throw new IllegalBankTradeException(recipient.getColour());
		}

		// If request doesn't match what the offer should give
		if(offerSum / reqSum != exchangeAmount)
		{
			throw new IllegalBankTradeException(recipient.getColour());
		}

		// Perform swap and return
		recipient.spendResources(offer);
		recipient.grantResources(request);
		return TradeStatusProto.ACCEPT;
	}

	/**
	 * If trade was successful, exchange of resources occurs here
	 * @param trade the trade object detailing the trade
	 * @return the response status
	 */
	public TradeStatusProto processPortTrade(PortTradeProto trade) throws IllegalPortTradeException, CannotAffordException
	{
		// Find the port and extract the trade's contents
		Player recipient = players.get(Colour.fromProto(trade.getPlayer()));
		Map<ResourceType, Integer> request = processResources(trade.getRequestResources());
		Map<ResourceType, Integer> offer = processResources(trade.getOfferResources());
		Port port = grid.getPort(trade.getPort());
		int offerSum = 0, reqSum = 0;
		int exchangeAmount = port.getExchangeAmount();

		// Check that the player can afford the offer
		if(!recipient.canAfford(offer))
		{
			throw new CannotAffordException(recipient.getResources(), offer);
		}

		// Check that requested trade is allowed
		for(ResourceType r : ResourceType.values())
		{
			// sum up total quantities of offer and request
			offerSum += offer.containsKey(r) ? offer.get(r) : 0;
			reqSum += request.containsKey(r) ? request.get(r) : 0;

			// If too little or too many resources for a trade on this port
			if(offer.containsKey(r) && offer.get(r) % exchangeAmount != 0)
				throw new IllegalPortTradeException(recipient.getColour(), port);
		}

		// If request doesn't match what the offer should give
		if(offerSum / reqSum != exchangeAmount)
		{
			throw new IllegalPortTradeException(recipient.getColour(), port);
		}

		// Exchange resources
		port.exchange(recipient, offer, request);

		return TradeStatusProto.ACCEPT;
	}

	/**
	 * If trade was successful, exchange of resources occurs here
	 * @param trade the trade object detailing the trade
	 * @param offererColour the offerer's colour
	 * @param recipientColour the recipient's colour
	 * @return the response status
	 */
	public SuccessFailResponse processPlayerTrade(PlayerTradeProto trade, Colour offererColour, Colour recipientColour)
	{
        SuccessFailResponse.Builder resp = SuccessFailResponse.newBuilder();

        // Find the recipient and extract the trade's contents
		ResourceCount offer = trade.getOffer();
		ResourceCount request = trade.getRequest();
		Player recipient = players.get(recipientColour), recipientCopy = recipient.copy();
		Player offerer = players.get(offererColour);

		try
		{
			// Exchange resources
			offerer.spendResources(offer);
			recipient.grantResources(offer);
			
			recipient.spendResources(request);
			offerer.grantResources(request);
        	resp.setResult(ResultProto.SUCCESS);
		}
		catch(CannotAffordException e)
		{
			// Reset recipient and throw exception. Offerer is reset in above method
			recipient.restoreCopy(recipientCopy, null);

			resp.setResult(ResultProto.FAILURE);
		}

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
	public UpgradeSettlementResponse upgradeSettlement(UpgradeSettlementRequest move, Colour playerColour) throws CannotAffordException, CannotUpgradeException
	{
		UpgradeSettlementResponse.Builder resp = UpgradeSettlementResponse.newBuilder();
        Player p = players.get(playerColour);
		Node n = grid.getNode(move.getPoint().getX(), move.getPoint().getY());
		
		// Try to upgrade settlement
		p.upgradeSettlement(n);

		resp.setNewBuilding(n.getSettlement().toProto());
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
	public BuildSettlementResponse buildSettlement(BuildSettlementRequest request, Colour playerColour) throws CannotAffordException, IllegalPlacementException, SettlementExistsException
	{
		BuildSettlementResponse.Builder resp = BuildSettlementResponse.newBuilder();
        Player p = players.get(playerColour);
        PointProto pointProto = request.getPoint();
		Node node = grid.getNode(pointProto.getX(), pointProto.getY());
		
		// Try to build settlement
		p.buildSettlement(node);
		
		// Check all combinations of edges to check if a road chain was broken
		for(int i = 0; i < node.getEdges().size(); i++)
		{
			boolean broken = false;
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
			if(broken)
			{
				checkLongestRoad();
				break;
			}
		}
		// TODO send out updated road counts

		resp.setNewBuilding(node.getSettlement().toProto());
        return resp.build();
	}
	
	/**
	 * Checks that the player can buy a development card
	 * @param type the type of development card to play
	 * @param playerColour the player's colour
	 * @return the response message to the client
	 */
	private SuccessFailResponse playDevelopmentCard(DevelopmentCardType type, Colour playerColour) throws DoesNotOwnException
	{
	    SuccessFailResponse.Builder resp = SuccessFailResponse.newBuilder();
	    Player p = players.get(playerColour);

		// Try to play card
		p.playDevelopmentCard(type);
		resp.setResult(ResultProto.SUCCESS);

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
        DevelopmentCardType card = p.buyDevelopmentCard(DevelopmentCardType.chooseRandom());
        resp.setDevelopmentCard(DevelopmentCardType.toProto(card));
		return resp.build();
	}

	/**
	 * Plays a knight development card if the player has one
	 * @param request the request
	 * @param playerColour the player
	 * @return a response object
	 * @throws CannotStealException if the player cannot steal from the requested player
	 * @throws DoesNotOwnException if the player does not own a Knight card
	 */
	public PlayKnightCardResponse playKnightCard(PlayKnightCardRequest request, Colour playerColour) throws CannotStealException, DoesNotOwnException
	{
		PlayKnightCardResponse.Builder resp = PlayKnightCardResponse.newBuilder();

		// Ensure the player has the dev card, and play it
		playDevelopmentCard(DevelopmentCardType.Knight, playerColour);

		// Perform the swap and take a resource
		resp.setMoveRobberResponse(moveRobber(request.getRequest(), playerColour));
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
                resp.setResource(ResourceType.toProto(players.get(currentPlayer).takeResource(other)));
				valid = true;
			}
		}
		
		// Cannot take from this player
		if(!valid) throw new CannotStealException(playerColour, otherColour);
		
		// return success message
		grid.swapRobbers(newHex);
		resp.setRobberLocation(point);
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
	public PlayRoadBuildingCardResponse playBuildRoadsCard(PlayRoadBuildingCardRequest request, Colour playerColour)
			throws CannotAffordException, CannotBuildRoadException, RoadExistsException, DoesNotOwnException
	{
		PlayRoadBuildingCardResponse.Builder resp = PlayRoadBuildingCardResponse.newBuilder();

		playDevelopmentCard(DevelopmentCardType.RoadBuilding, currentPlayer);

		// Set responses and return
		resp.setResponse1(buildRoad(request.getRequest1(), playerColour));
		resp.setResponse2(buildRoad(request.getRequest2(), playerColour));
		return resp.build();
	}
	
	/**
	 * Process the playing of the 'University' development card.
	 * @return return message
	 */
	public SuccessFailResponse playUniversityCard() throws DoesNotOwnException
	{
        SuccessFailResponse.Builder resp = SuccessFailResponse.newBuilder();

        playDevelopmentCard(DevelopmentCardType.University, currentPlayer);
        grantVpPoint();

        resp.setResult(ResultProto.SUCCESS);
        return resp.build();
	}
	
	/**
	 * Process the playing of the 'Library' development card.
	 * @return return message
	 */
	public SuccessFailResponse playLibraryCard() throws DoesNotOwnException
	{
        SuccessFailResponse.Builder resp = SuccessFailResponse.newBuilder();

		playDevelopmentCard(DevelopmentCardType.Library, currentPlayer);
		grantVpPoint();

        resp.setResult(ResultProto.SUCCESS);
        return resp.build();
	}
	
	/**
	 * Process the playing of the 'Year of Plenty' development card.
	 * @param request the request to process
	 * @return return message
	 */
	public PlayYearOfPlentyCardResponse playYearOfPlentyCard(PlayYearOfPlentyCardRequest request) throws DoesNotOwnException
	{
		PlayYearOfPlentyCardResponse.Builder resp = PlayYearOfPlentyCardResponse.newBuilder();

		playDevelopmentCard(DevelopmentCardType.YearOfPlenty, currentPlayer);

		// Set up grant
		Map<ResourceType, Integer> grant = new HashMap<ResourceType, Integer>();
		grant.put(ResourceType.fromProto(request.getR1()), 1);
		grant.put(ResourceType.fromProto(request.getR2()), 1);
		players.get(currentPlayer).grantResources(grant);

		resp.setR1(request.getR1());
		resp.setR2(request.getR2());
		return resp.build();
	}
	
	/**
	 * Process the playing of the 'Monopoly' development card.
	 * @param request the request to process
	 * @return return message
	 */
	public PlayMonopolyCardResponse playMonopolyCard(PlayMonopolyCardRequest request) throws DoesNotOwnException
	{
        PlayMonopolyCardResponse.Builder response = PlayMonopolyCardResponse.newBuilder();
		ResourceTypeProto r = request.getResource();
		Map<ResourceType, Integer> grant = new HashMap<ResourceType, Integer>();
		int sum = 0;

		playDevelopmentCard(DevelopmentCardType.Monopoly, currentPlayer);

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
			players.get(currentPlayer).grantResources(grant);
		}
		
		// Return message is string showing number of resources taken
        response.setNumResources(sum);
		response.setResource(r);
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
        response.setNewRoad(edge.getRoad().toProto());
		return response.build();
	}
	
	/**
	 * Checks who has the longest road
	 */
	private void checkLongestRoad()
	{
		Player current = players.get(currentPlayer);
		Player playerWithLongestRoad = players.get(this.playerWithLongestRoad);

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
			this.playerWithLongestRoad = currentPlayer;
			current.setHasLongestRoad(true);
		}
	}

	/**
	 * @return a representation of the board that is compatible with protofbufs
	 */
	public GiveBoardResponse getBoard()
	{
		GiveBoardResponse.Builder resp = GiveBoardResponse.newBuilder();
		BoardProtos.BoardProto.Builder builder = BoardProtos.BoardProto.newBuilder();
		int index = 0;

		// Add edges
		for(Edge e : getGrid().getEdgesAsList())
		{
			builder.addEdgesBuilder();
			builder.setEdges(index++, e.toEdgeProto());
		}

		// Add hexes
		index = 0;
		for(Hex h : getGrid().getHexesAsList())
		{
			builder.addHexesBuilder();
			builder.setHexes(index++, h.toHexProto());
		}

		// Add port
		index = 0;
		for(Port p : getGrid().getPortsAsList())
		{
			builder.addPortsBuilder();
			builder.setPorts(index++, p.toPortProto());
		}

		// Add nodes
		index = 0;
		for(Node n : getGrid().getNodesAsList())
		{
			builder.addNodesBuilder();
			builder.setNodes(index++, n.toProto());
		}

		resp.setBoard(builder.build());
		return resp.build();
	}

	/**
	 * Toggles a player's turn
	 * @return 
	 */
	public EndMoveResponse changeTurn()
	{
		EndMoveResponse.Builder resp = EndMoveResponse.newBuilder();

		setCurrentPlayer(getPlayersAsList()[++current % NUM_PLAYERS].getColour());
		resp.setNewTurn(Colour.toProto(currentPlayer));

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

	private void grantVpPoint()
	{
		Player currentPlayer = players.get(this.currentPlayer);
		currentPlayer.setVp(currentPlayer.getVp() + 1);
	}

	public void restorePlayerFromCopy(Player copy, DevelopmentCardType card)
	{
		players.get(copy.getColour()).restoreCopy(copy, card);
	}
}
