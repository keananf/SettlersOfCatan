package enums;

import protocol.EnumProtos.DevelopmentCardProto;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public enum DevelopmentCardType
{
	Knight, // Steal 1 resource from
	Library, // 1 VP
	University, // 1 VP
	YearOfPlenty, // Gain any 2 resources from the bank
	RoadBuilding, // Build two new roads
	Monopoly; // Every player must give over all resources of a particular type

	private static Random rand;

	static
	{
		rand = new Random();
	}

	public static DevelopmentCardProto toProto(DevelopmentCardType card)
	{
		DevelopmentCardProto proto = DevelopmentCardProto.KNIGHT;

		switch (card) // TODO complete
		{
		case Knight:
			proto = DevelopmentCardProto.KNIGHT;
			break;

		case Library:
			proto = DevelopmentCardProto.LIBRARY;
			break;

		case Monopoly:
			proto = DevelopmentCardProto.MONOPOLY;
			break;

		case RoadBuilding:
			proto = DevelopmentCardProto.ROAD_BUILDING;
			break;

		case YearOfPlenty:
			proto = DevelopmentCardProto.YEAR_OF_PLENTY;
			break;

		case University:
			proto = DevelopmentCardProto.UNIVERSITY;
			break;
		}

		return proto;
	}

	public static DevelopmentCardType fromProto(DevelopmentCardProto type)
	{
		DevelopmentCardType card = null;

		// Switch on type
		switch (type)
		{
		case KNIGHT:
			card = DevelopmentCardType.Knight;
			break;
		case LIBRARY:
			card = DevelopmentCardType.Library;
			break;
		case MONOPOLY:
			card = DevelopmentCardType.Monopoly;
			break;
		case ROAD_BUILDING:
			card = DevelopmentCardType.RoadBuilding;
			break;
		case UNIVERSITY:
			card = DevelopmentCardType.University;
			break;
		case YEAR_OF_PLENTY:
			card = DevelopmentCardType.YearOfPlenty;
			break;
		}
		return card;
	}

	/**
	 * @return a map containing the total cost for all resources
	 */
	public static Map<ResourceType, Integer> getCardCost()
	{
		Map<ResourceType, Integer> resources = new HashMap<ResourceType, Integer>();

		resources.put(ResourceType.Ore, 1);
		resources.put(ResourceType.Grain, 1);
		resources.put(ResourceType.Wool, 1);

		return resources;
	}

	public static DevelopmentCardType chooseRandom()
	{
		// Randomly choose a development card to allocate
		return DevelopmentCardType.values()[rand.nextInt(DevelopmentCardType.values().length)];
	}
}
