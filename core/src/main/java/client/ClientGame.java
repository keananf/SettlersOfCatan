package client;

import enums.Colour;
import enums.DevelopmentCardType;
import enums.ResourceType;
import exceptions.*;
import game.Game;
import game.build.City;
import game.build.Road;
import game.build.Settlement;
import game.players.LocalPlayer;
import game.players.Player;
import grid.*;
import intergroup.board.Board;
import intergroup.lobby.Lobby;
import intergroup.resource.Resource;
import intergroup.trade.Trade;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A game with additional methods for processing protobufs for the client
 * Created by 140001596
 */
public class ClientGame extends Game
{
    private boolean gameOver;
    private int dice;
    private Map<Colour, Integer> boughtDevCards;
    private Map<Colour, HashMap<DevelopmentCardType, Integer>> playedDevCards;
    private Player thisPlayer;
    private ChatBoard chatBoard;
    private List<String> usersInLobby;

    public ClientGame()
    {
        super();
        boughtDevCards = new HashMap<Colour, Integer>();
        playedDevCards = new HashMap<Colour, HashMap<DevelopmentCardType, Integer>>();
        chatBoard = new ChatBoard();
        usersInLobby = new ArrayList<String>(NUM_PLAYERS);

        // Instantiate the playedDevCards maps
        for(Colour c : Colour.values())
        {
            playedDevCards.put(c, new HashMap<DevelopmentCardType, Integer>());

            for(DevelopmentCardType d : DevelopmentCardType.values())
            {
                playedDevCards.get(c).put(d, 0);
            }
        }
    }

    /**
     * @return a representation of the board that is compatible with protofbufs
     * @param beginGame
     */
    public HexGrid setBoard(Lobby.GameSetup beginGame) throws InvalidCoordinatesException, CannotAffordException
    {
        HexGrid grid = new HexGrid();
        List<Hex> hexes = processHexes(beginGame.getHexesList());
        List<Port> ports = processPorts(beginGame.getHarboursList());
        processPlayerSettings(beginGame.getOwnPlayer(), beginGame.getPlayerSettingsList());

        // Overwrite current grid
        grid.setNodesAndHexes(hexes);
        grid.setPorts(ports);

        return grid;
    }

    /**
     * Loads in all the player information
     * @param ownPlayer this player's information
     * @param playerSettingsList the list of other players' information
     */
    private void processPlayerSettings(Board.Player ownPlayer, List<Lobby.GameSetup.PlayerSetting> playerSettingsList)
    {
        // Load in each player's info
        for(Lobby.GameSetup.PlayerSetting player : playerSettingsList)
        {
            enums.Colour col = enums.Colour.fromProto(player.getColour());
            LocalPlayer newPlayer = new LocalPlayer(col, player.getUsername());
            newPlayer.setId(player.getPlayer().getId());

            // Check if it is this player
            if(player.getPlayer().getId().equals(ownPlayer.getId()))
            {
                thisPlayer = newPlayer;
                players.put(col, thisPlayer);
            }
            else
            {
                players.put(col, newPlayer);
            }

            // Add mapping from colours to ids
            idsToColours.put(player.getPlayer().getId(), col);
        }
    }

    /**
     * Retrieve the hex objects referred to by the proto
     * @param protos the hex protos
     */
    private List<Hex> processHexes(List<Board.Hex> protos)
    {
        List<Hex> hexes = new ArrayList<Hex>();

        // Add nodes
        for(Board.Hex proto : protos)
        {
            hexes.add(Hex.fromProto(proto));
        }

        return hexes;
    }

    /**
     * Retrieve the port objects referred to by the proto
     * @param protos the port protos
     */
    private List<Port> processPorts(List<Board.Harbour> protos)
    {
        List<Port> ports = new ArrayList<Port>();

        // Add ports
        for(Board.Harbour harbour : protos)
        {
            Board.Edge e = harbour.getLocation();
            Board.Point p1 = e.getA(), p2 = e.getB();
            Port port = new Port(grid.getNode(p1.getX(), p1.getY()), grid.getNode(p2.getX(), p2.getY()));
            port.setExchangeType(ResourceType.fromProto(harbour.getResource()));

            ports.add(port);
        }

        return ports;
    }

    /**
     * Processes the user who just joined the lobby
     * @param lobbyUpdate the list of players in the lobby
     * @param instigator
     */
    public void processPlayers(Lobby.Usernames lobbyUpdate, Board.Player instigator)
    {
        for(String username : lobbyUpdate.getUsernameList())
        {
            if(!usersInLobby.contains(username))
            {
                usersInLobby.add(username);
            }
        }
    }

    /**
     * @return if the game has ended or not
     */
    public boolean isOver()
    {
        return gameOver;
    }

    /**
     * Once called, the listener thread will terminate the connection to the server
     */
    public void setGameOver()
    {
        gameOver = true;
    }

    /**
     * Writes the given string to the chatBoard
     * @param chatMessage
     */
    public void writeMessage(String chatMessage, Board.Player instigator)
    {
        Player p = getPlayer(instigator.getId());
        chatBoard.writeMessage(chatMessage, p.getUsername(), p.getColour());
    }

    /**
     * Updates the dice roll and allocate resources
     * @param dice the new dice roll
     * @param resourceAllocationList
     */
    public void processDice(int dice, List<Board.ResourceAllocation> resourceAllocationList)
    {
        this.dice = dice;

        if(dice != 7)
        {
            // For each player's new resources
            for(Board.ResourceAllocation alloc : resourceAllocationList)
            {
                Map<ResourceType, Integer> grant = processResources(alloc.getResources());
                Player p = getPlayer(alloc.getPlayer().getId());

                p.grantResources(grant);
            }
        }
    }

    /**
     * Swap the robber to the given point received from the server
     * @param robberMove the robber's new position
     */
    public void moveRobber(Board.Point robberMove) throws InvalidCoordinatesException
    {
        Hex hex = grid.getHex(robberMove.getX(), robberMove.getY());

        // If invalid coordinates
        if(hex == null)
        {
            throw new InvalidCoordinatesException(robberMove.getX(), robberMove.getY());
        }

        grid.swapRobbers(hex);
    }

    /**
     * Adds the new road received from the server to the board
     * @param newRoad the new road to add.
     * @param instigator the instigator who instigated the event
     */
    public Road processRoad(Board.Edge newRoad, Board.Player instigator) throws RoadExistsException,
            CannotBuildRoadException, CannotAffordException
    {
        // Extract information and find edge
        Edge newEdge = grid.getEdge(newRoad.getA(), newRoad.getB());
        Player player = getPlayer(instigator.getId());

        // Spend resources if it is this instigator
        player.spendResources(Road.getRoadCost());

        // Make new road object
        Road r = ((LocalPlayer)players.get(player.getColour())).addRoad(newEdge);
        checkLongestRoad(false);

        return r;
    }

    /**
     * Processes a new bulding, and adds it to the board
     * @param city the new city
     * @param instigator the person who built the city
     * @return the new city
     */
    public City processNewCity(Board.Point city, Board.Player instigator, boolean setUp) throws InvalidCoordinatesException, CannotAffordException
    {
        // Extract information
        Node node = grid.getNode(city.getX(), city.getY());
        Player player = getPlayer(instigator.getId());

        // If invalid coordinates
        if(node == null || (node.getSettlement() != null && node.getSettlement() instanceof City))
        {
            throw new InvalidCoordinatesException(city.getX(), city.getY());
        }

        // Spend resources
        player.spendResources(City.getCityCost());

        // Create and add the city
        City c = new City(node, player.getColour());

        // Updates settlement and score
        players.get(player.getColour()).addSettlement(c);
        return c;
    }

    /**
     * Processes a new bulding, and adds it to the board
     * @param settlement the new settlement
     * @param instigator the person who built the settlement
     * @return the new settlement
     */
    public Settlement processNewSettlement(Board.Point settlement, Board.Player instigator, boolean setUp)
            throws InvalidCoordinatesException, CannotAffordException
    {
        // Extract information
        Node node = grid.getNode(settlement.getX(), settlement.getY());
        Player player = getPlayer(instigator.getId());

        // If invalid coordinates
        if(node == null || (node.getSettlement() != null && node.getSettlement() instanceof Settlement))
        {
            throw new InvalidCoordinatesException(settlement.getX(), settlement.getY());
        }

        // Spend resources
        player.spendResources(Settlement.getSettlementCost());

        // Create and add the settlement
        Settlement s = new Settlement(node, player.getColour());
        checkIfRoadBroken(node);

        // Updates settlement and score
        players.get(player.getColour()).addSettlement(s);
        return s;
    }

    /**
     * Records the played dev card for the given player
     * @param playedDevCard the played card
     */
    public void processPlayedDevCard(Board.PlayableDevCard playedDevCard, Board.Player instigator) throws DoesNotOwnException
    {
        DevelopmentCardType card = DevelopmentCardType.fromProto(playedDevCard);
        Map<DevelopmentCardType, Integer> playedCards = playedDevCards.get(currentPlayer);
        Player player = getPlayer(instigator.getId());

        // Record card being played
        int existing = playedCards.get(card);
        playedCards.put(card, existing + 1);

        // Eliminate one bought dev card from player
        existing = boughtDevCards.containsKey(player.getColour()) ? boughtDevCards.get(player.getColour()) : 0;
        if(existing > 0)
        {
            boughtDevCards.put(player.getColour(), existing - 1);
        }
        else throw new DoesNotOwnException(card, player.getColour());

        // Update largest army
        if(card.equals(DevelopmentCardType.Knight))
        {
            players.get(player.getColour()).addKnightPlayed();
            checkLargestArmy();
        }
    }

    /**
     * Records that the given player bought a dev card
     * @param boughtDevCard the bought dev card
     * @param instigator the player who caused the event
     */
    public void recordDevCard(Board.DevCard boughtDevCard, Board.Player instigator) throws CannotAffordException
    {
        Player player = getPlayer(instigator.getId());
        player.spendResources(DevelopmentCardType.getCardCost());

        // Spend resources if it is this player
        if(player.getColour().equals(thisPlayer.getColour()))
        {
            thisPlayer.addDevelopmentCard(boughtDevCard);
        }

        // Update number of dev cards each player is known to have
        Colour c = player.getColour();
        int existing = boughtDevCards.containsKey(c) ? boughtDevCards.get(c) : 0;
        boughtDevCards.put(c, existing + 1);
    }

    /**
     * Processes a bank trade
     * @param bankTrade the bank trade
     */
    public void processBankTrade(Trade.WithBank bankTrade, Board.Player instigator) throws CannotAffordException
    {
        Map<ResourceType, Integer> offering = processResources(bankTrade.getOffering());
        Map<ResourceType, Integer> wanting = processResources(bankTrade.getWanting());
        Player p = getPlayer(instigator.getId());

        // Update resources
        p.spendResources(offering);
        p.grantResources(wanting);
    }

    /**
     * Processes a player trade
     * @param playerTrade the player trade
     */
    public void processPlayerTrade(Trade.WithPlayer playerTrade, Board.Player sender) throws CannotAffordException
    {
        Map<ResourceType, Integer> offering = processResources(playerTrade.getOffering());
        Map<ResourceType, Integer> wanting = processResources(playerTrade.getWanting());
        Player instigator = getPlayer(sender.getId());
        Player recipient = getPlayer(playerTrade.getOther().getId());

        // Update resources
        instigator.spendResources(offering);
        instigator.grantResources(wanting);
        recipient.spendResources(wanting);
        recipient.grantResources(offering);
    }

    /**
     * Processes the cards this player had to discard
     * @param cardsDiscarded the discarded resources
     * @param instigator the player who discarded them
     */
    public void processDiscard(Resource.Counts cardsDiscarded, Board.Player instigator) throws CannotAffordException
    {
        Player p = getPlayer(instigator.getId());
        p.spendResources(processResources(cardsDiscarded));
    }

    /**
     * Processes the cards this player had to discard
     * @param steal the stolen resources
     * @param instigator the player who stole them
     */
    public void processResourcesStolen(Board.Steal steal, Board.Player instigator) throws CannotAffordException
    {
        Player p = getPlayer(instigator.getId());
        Player p2 = getPlayer(steal.getVictim().getId());
        ResourceType r = ResourceType.fromProto(steal.getResource());
        int quantity = steal.getQuantity();

        // Update resources
        Map<ResourceType, Integer> stolen = new HashMap<ResourceType, Integer>();
        stolen.put(r, quantity);
        p2.spendResources(stolen);
        p.grantResources(stolen);

    }

    /**
     * Processes the cards this player had to discard
     * @param multiSteal the resources taken from each user
     * @param instigator the player who stole them
     */
    public void processMonopoly(Board.MultiSteal multiSteal, Board.Player instigator) throws CannotAffordException
    {
        Player p = getPlayer(instigator.getId());

        // For each player stolen from
        for(Board.Steal steal : multiSteal.getTheftsList())
        {
            Player p2 = getPlayer(steal.getVictim().getId());
            ResourceType r = ResourceType.fromProto(steal.getResource());
            int quantity = steal.getQuantity();

            // Update resources
            Map<ResourceType, Integer> stolen = new HashMap<ResourceType, Integer>();
            stolen.put(r, quantity);
            p2.spendResources(stolen);
            p.grantResources(stolen);
        }
    }

    /**
     * Processes the cards this player had to discard
     * @param resource the stolen resources
     * @param instigator the player who stole them
     */
    public void processResourceChosen(Resource.Kind resource, Board.Player instigator)
    {
        Player p = getPlayer(instigator.getId());
        Map<ResourceType, Integer> map = new HashMap<ResourceType, Integer>();
        map.put(ResourceType.fromProto(resource), 1);

        p.grantResources(map);
    }

    /**
     * Return the current dice roll
     * @return
     */
    public int getDice()
    {
        return dice;
    }

    /**
     * Return the total amounts of dev cards owned by each player
     * @return
     */
    public Map<Colour, Integer> getBoughtDevCards()
    {
        return boughtDevCards;
    }

    /**
     * @return the map of played dev cards
     */
    public Map<Colour,HashMap<DevelopmentCardType,Integer>> getPlayedDevCards()
    {
        return playedDevCards;
    }

    /**
     * @return this player
     */
    public Player getPlayer()
    {
        return thisPlayer;
    }

}
