package enums;

import protocol.EnumProtos.*;

public enum ResourceType
{
	Generic, // Default
	Wool,
	Ore,
	Grain, 
	Brick,
	Lumber,;

    public static ResourceType fromProto(ResourceTypeProto r)
	{
		ResourceType resource = ResourceType.Brick;

		switch(r)
		{
			case BRICK:
				resource = ResourceType.Brick;
				break;

			case WOOL:
				resource = ResourceType.Wool;
				break;

			case LUMBER:
				resource = ResourceType.Lumber;
				break;

			case ORE:
				resource = ResourceType.Ore;
				break;

			case GRAIN:
				resource = ResourceType.Grain;
				break;
		}

		return resource;
    }

	public static ResourceTypeProto toProto(ResourceType r)
	{
		ResourceTypeProto resource = ResourceTypeProto.BRICK;

		switch(r)
		{
			case Brick:
				resource = ResourceTypeProto.BRICK;
				break;

			case Wool:
				resource = ResourceTypeProto.WOOL;
				break;

			case Lumber:
				resource = ResourceTypeProto.LUMBER;
				break;

			case Ore:
				resource = ResourceTypeProto.ORE;
				break;

			case Grain:
				resource = ResourceTypeProto.GRAIN;
				break;
		}

		return resource;
	}
}
