package enums;

import protocol.EnumProtos.*;

public enum ResourceType
{
	Generic ("desert.g3db"), // Default
	Wool    ("grass.g3db"),
	Ore     ("mountain.g3db"),
	Grain   ("grain.g3db"),
	Brick   ("mine.g3db"),
	Lumber  ("forest.g3db");

	public final String modelPath;
	ResourceType(final String modelPath)
	{
		this.modelPath = modelPath;
	}

	public static ResourceType fromProto(ResourceTypeProto r)
	{
		ResourceType resource = ResourceType.Brick;

		switch (r)
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

		case GENERIC:
			resource = ResourceType.Generic;
			break;
		}

		return resource;
	}

	public static ResourceTypeProto toProto(ResourceType r)
	{
		ResourceTypeProto resource = ResourceTypeProto.BRICK;

		switch (r)
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

		case Generic:
			resource = ResourceTypeProto.GENERIC;
			break;
		}

		return resource;
	}
}
