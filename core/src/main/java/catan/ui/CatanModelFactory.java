package catan.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
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
	private final Model HEX;
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
		HEX = builder.createCylinder(2.2f, 0.2f, 2.2f, 6, new Material(), DEFAULT_ATTRS);
		GRAIN = assets.getModel("grain.g3db") ;
		ORE = assets.getModel("ore.g3db");
		WOOL = assets.getModel("lumber.g3db");
		LUMBER = assets.getModel("lumber.g3db");
		GENERIC = assets.getModel("desert.g3db");
		BRICK = assets.getModel("Mine.g3db");
	}

	ModelInstance getSeaInstance()
	{
		return new ModelInstance(SEA, ORIGIN);
	}

	ModelInstance getIslandInstance()
	{
		return new ModelInstance(ISLAND, ORIGIN);
	}

	ModelInstance getHexInstance(final Vector3 pos, final ResourceType type)
	{
		final ModelInstance instance = new ModelInstance(HEX, pos);
		instance.transform.rotate(0, 1, 0, 90f);
		instance.materials.get(0).set(ColorAttribute.createDiffuse(resourceTypeToColor(type)));
		return instance;
	}

	private Color resourceTypeToColor(ResourceType type)
	{
		switch (type)
		{
		case Generic:
			return Color.FIREBRICK;
		case Wool:
			return Color.FIREBRICK;
		case Ore:
			return Color.FIREBRICK;
		case Grain:
			return Color.FIREBRICK;
		case Brick:
			return Color.FIREBRICK;
		case Lumber:
			return Color.FIREBRICK;
		default:
			return null;
		}
	}
	
	ModelInstance getTerrainInstance(ResourceType type, Vector3 pos) {
		Model model = null;
		switch (type) {
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
			break;

		}
		
		ModelInstance mod = new ModelInstance(model, pos);
		mod.transform.translate(0, 0.2f, 0);
		mod.transform.scale(1.6f, 1, 1f);
		return mod;
	}
	
	
	
	
}
