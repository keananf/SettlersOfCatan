package enums;

import intergroup.resource.Resource;
import intergroup.terrain.Terrain;

import java.util.Random;

public enum ResourceType
{
	Generic("desert.g3db"), // Default
	Wool("grass.g3db"), Ore("mountain.g3db"), Grain("grain.g3db"), Brick("mine.g3db"), Lumber("forest.g3db");

	public final String modelPath;

	ResourceType(final String modelPath)
	{
		this.modelPath = modelPath;
	}

	private static Random rand;
	static
	{
		rand = new Random();
	}

	public static ResourceType fromProto(Resource.Kind r)
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

	public static Resource.Kind toProto(ResourceType r)
	{
		Resource.Kind resource = Resource.Kind.BRICK;

		switch (r)
		{
		case Brick:
			resource = Resource.Kind.BRICK;
			break;

		case Wool:
			resource = Resource.Kind.WOOL;
			break;

		case Lumber:
			resource = Resource.Kind.LUMBER;
			break;

		case Ore:
			resource = Resource.Kind.ORE;
			break;

		case Grain:
			resource = Resource.Kind.GRAIN;
			break;

		case Generic:
			resource = Resource.Kind.GENERIC;
			break;
		}

		return resource;
	}

	/**
	 * @return the terrain associated with the given resource
	 */
	public static Terrain.Kind getTerrainFromResource(ResourceType res)
	{
		Terrain.Kind terrain = Terrain.Kind.DESERT;

		switch (res)
		{
		case Generic:
			terrain = Terrain.Kind.DESERT;
			break;
		case Grain:
			terrain = Terrain.Kind.FIELDS;
			break;
		case Lumber:
			terrain = Terrain.Kind.FOREST;
			break;
		case Brick:
			terrain = Terrain.Kind.HILLS;
			break;
		case Ore:
			terrain = Terrain.Kind.MOUNTAINS;
			break;
		case Wool:
			terrain = Terrain.Kind.PASTURE;
			break;
		}
		return terrain;
	}

	/**
	 * @return the resource associated with the given terrain
	 */
	public static ResourceType getResourceFromTerrain(Terrain.Kind terrain)
	{
		ResourceType r = ResourceType.Generic;

		switch (terrain)
		{
		case DESERT:
			r = ResourceType.Generic;
			break;
		case FIELDS:
			r = ResourceType.Grain;
			break;
		case FOREST:
			r = ResourceType.Lumber;
			break;
		case HILLS:
			r = ResourceType.Brick;
			break;
		case MOUNTAINS:
			r = ResourceType.Ore;
			break;
		case PASTURE:
			r = ResourceType.Wool;
			break;
		}
		return r;
	}

	/**
	 * @return a random resource type
	 */
	public static ResourceType random()
	{
		return ResourceType.values()[rand.nextInt(ResourceType.values().length)];
	}
}
