package enums;

import game.build.DevelopmentCard;
import protocol.EnumProtos.*;

public enum DevelopmentCardType
{
	Knight,         // Steal 1 resource from
	Library,        // 1 VP
	University,     // 1 VP
	YearOfPlenty,   // Gain any 2 resources from the bank
	RoadBuilding,   // Build two new roads
	Monopoly;       // Every player must give over all resources of a particular type

    public static DevelopmentCardProto toProto(DevelopmentCardType card)
	{
	    DevelopmentCardProto proto = DevelopmentCardProto.KNIGHT;

		switch(card) // TODO complete
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
}
