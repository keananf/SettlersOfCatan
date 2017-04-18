package enums;

import exceptions.BankLimitException;
import game.Bank;
import intergroup.board.Board;
import intergroup.board.Board.PlayableDevCard;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public enum DevelopmentCardType
{
	Knight, // Steal 1 resource from
	Library, // 1 VP
	University, // 1 VP
	Chapel, // 1 VP
	Market, // 1 VP
	Palace, // 1 VP
	YearOfPlenty, // Gain any 2 resources from the bank
	RoadBuilding, // Build two new roads
	Monopoly; // Every player must give over all resources of a particular type

	private static final Random rand;

	static
	{
		rand = new Random();
	}

	public static Board.DevCard toProto(DevelopmentCardType card)
	{
		Board.DevCard.Builder devCard = Board.DevCard.newBuilder();

		switch (card)
		{
		case Knight:
			devCard.setPlayableDevCard(PlayableDevCard.KNIGHT);
			break;
		case Monopoly:
			devCard.setPlayableDevCard(PlayableDevCard.MONOPOLY);
			break;
		case RoadBuilding:
			devCard.setPlayableDevCard(PlayableDevCard.ROAD_BUILDING);
			break;
		case YearOfPlenty:
			devCard.setPlayableDevCard(PlayableDevCard.YEAR_OF_PLENTY);
			break;
		case Library:
			devCard.setVictoryPoint(Board.VictoryPoint.LIBRARY);
			break;
		case University:
			devCard.setVictoryPoint(Board.VictoryPoint.UNIVERSITY);
			break;
		case Chapel:
			devCard.setVictoryPoint(Board.VictoryPoint.CHAPEL);
			break;
		case Palace:
			devCard.setVictoryPoint(Board.VictoryPoint.PALACE);
			break;
		case Market:
			devCard.setVictoryPoint(Board.VictoryPoint.MARKET);
			break;
		}

		return devCard.build();
	}

	/**
	 * Converts between a dev card received across the network and the internal
	 * representation
	 * 
	 * @param devCard the dev card
	 * @return the corresponding type
	 */
	public static DevelopmentCardType fromProto(Board.DevCard devCard)
	{
		DevelopmentCardType type = null;

		// Switch on overarching card type
		switch (devCard.getCardCase())
		{
		case VICTORYPOINT:
		{
			// switch on type of card granting VP
			switch (devCard.getVictoryPoint())
			{
			case LIBRARY:
				type = DevelopmentCardType.Library;
				break;
			case UNIVERSITY:
				type = DevelopmentCardType.University;
				break;
			case CHAPEL:
				type = DevelopmentCardType.Chapel;
				break;
			case MARKET:
				type = DevelopmentCardType.Market;
				break;
			case PALACE:
				type = DevelopmentCardType.Palace;
				break;
			}
			break;
		}

		case PLAYABLEDEVCARD:
		{
			// Switch on playable card type
			switch (devCard.getPlayableDevCard())
			{
			case KNIGHT:
				type = DevelopmentCardType.Knight;
				break;
			case MONOPOLY:
				type = DevelopmentCardType.Monopoly;
				break;
			case ROAD_BUILDING:
				type = DevelopmentCardType.RoadBuilding;
				break;
			case YEAR_OF_PLENTY:
				type = DevelopmentCardType.YearOfPlenty;
				break;
			}
			break;
		}
		}
		return type;
	}

	/**
	 * @return a map containing the total cost for all resources
	 */
	public static Map<ResourceType, Integer> getCardCost()
	{
		Map<ResourceType, Integer> resources = new HashMap<>();

		resources.put(ResourceType.Ore, 1);
		resources.put(ResourceType.Grain, 1);
		resources.put(ResourceType.Wool, 1);

		return resources;
	}

	public static DevelopmentCardType chooseRandom(Bank bank) throws BankLimitException
	{
		DevelopmentCardType type = null;
		if (bank.getNumAvailableDevCards() == 0) { throw new BankLimitException("Not enough dev cards left"); }

		// Randomly choose a development card to allocate
		while (type == null || bank.getAvailableDevCards().get(type) == 0)
		{
			type = DevelopmentCardType.values()[rand.nextInt(DevelopmentCardType.values().length)];
		}

		// Eliminate from bank
		bank.subtractAvailableDevCards(type);
		return type;
	}

	public static DevelopmentCardType fromProto(PlayableDevCard playedDevCard)
	{
		if (playedDevCard.equals(PlayableDevCard.KNIGHT)) return DevelopmentCardType.Knight;

		if (playedDevCard.equals(PlayableDevCard.MONOPOLY)) return DevelopmentCardType.Monopoly;

		if (playedDevCard.equals(PlayableDevCard.ROAD_BUILDING)) return DevelopmentCardType.RoadBuilding;

		// if(playedDevCard.equals(PlayableDevCard.YEAR_OF_PLENTY))
		return DevelopmentCardType.YearOfPlenty;
	}
}
