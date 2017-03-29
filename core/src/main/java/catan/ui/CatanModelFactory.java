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

	CatanModelFactory()
	{
		final ModelBuilder builder = new ModelBuilder();
		SEA = builder.createCylinder(150f, 0.01f, 150f, 6, WATER, DEFAULT_ATTRS);
		ISLAND = builder.createCylinder(11f, 0.1f, 11f, 6, DIRT, DEFAULT_ATTRS);
		HEX = builder.createCylinder(2.2f, 0.2f, 2.2f, 6, new Material(), DEFAULT_ATTRS);
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
			return Color.BLACK;
		case Wool:
			return Color.WHITE;
		case Ore:
			return Color.BLUE;
		case Grain:
			return Color.YELLOW;
		case Brick:
			return Color.FIREBRICK;
		case Lumber:
			return Color.FOREST;
		default:
			return null;
		}
	}
}
