package catan.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import enums.ResourceType;

class CatanModelFactory
{
	private static final long DEFAULT_ATTRS = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal
			| VertexAttributes.Usage.TextureCoordinates;
	private static final Vector3 ORIGIN = new Vector3(0, 0, 0);

	private static final Material WATER = new Material(
			TextureAttribute.createDiffuse(new Texture(Gdx.files.internal("textures/water.jpg"))));
	private static final Material DIRT = new Material(
			TextureAttribute.createDiffuse(new Texture(Gdx.files.internal("textures/dirt.png"))));

	private final Model SEA;
	private final Model ISLAND;
	private final Model GRAIN;
	private final Model ORE;
	private final Model WOOL;
	private final Model LUMBER;
	private final Model BRICK;
	private final Model GENERIC;

	CatanModelFactory(AssMan assets)
	{
		final ModelBuilder builder = new ModelBuilder();
		SEA = builder.createCylinder(150f, 0.01f, 150f, 6, WATER, DEFAULT_ATTRS);
		ISLAND = builder.createCylinder(11f, 0.1f, 11f, 6, DIRT, DEFAULT_ATTRS);
		GRAIN = assets.getModel("grain.g3db");
		ORE = assets.getModel("ore.g3db");
		WOOL = assets.getModel("Lumber.g3db");
		LUMBER = assets.getModel("Lumber.g3db");
		GENERIC = assets.getModel("Desert.g3db");
		BRICK = assets.getModel("Mine2.g3db");
	}

	ModelInstance getSeaInstance()
	{
		
		
		ModelInstance sea = new ModelInstance(SEA, ORIGIN);
		sea.transform.translate(0,-0.1f,0);
		return sea;
	}

	ModelInstance getIslandInstance()
	{
		ModelInstance island = new ModelInstance(ISLAND, ORIGIN);
		island.transform.translate(0,-0.1f,0);
		return island;
	}

	ModelInstance getTerrainInstance(ResourceType type, Vector3 pos)
	{
		final Model model;
		switch (type)
		{
		case Brick:
			model = BRICK;
			break;
		case Generic:
			model = GENERIC;
			break;
		case Grain:
			model = GRAIN;
			break;
		case Lumber:
			model = LUMBER;
			break;
		case Ore:
			model = ORE;
			break;
		case Wool:
			model = WOOL;
			break;
		default:
			return null;
		}

		final ModelInstance mod = new ModelInstance(model, pos);
		mod.transform.translate(0, 0.1f, 0);
		return mod;
	}
}
