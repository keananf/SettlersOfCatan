package catan.ui;

import catan.SettlersOfCatan;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import enums.Colour;
import game.build.Building;
import game.build.City;
import game.build.Road;
import game.build.Settlement;
import grid.Hex;

import java.util.Set;

class CatanModelFactory
{
	private static final long DEFAULT_ATTRS
			= VertexAttributes.Usage.Position
			| VertexAttributes.Usage.Normal
			| VertexAttributes.Usage.TextureCoordinates;
	private static final Vector3 ORIGIN = new Vector3(0, 0, 0);
	private static final float GROUND_LEVEL = 0.55f;

	private static final Material WATER = new Material(
			TextureAttribute.createDiffuse(new Texture(Gdx.files.internal("textures/water.jpg"))));
	private static final Material DIRT = new Material(
			TextureAttribute.createDiffuse(new Texture(Gdx.files.internal("textures/dirt.png"))));

	private static final BitmapFont font = new BitmapFont();
	private static final BitmapFont.BitmapFontData data = font.getData();
	private static final Pixmap fontPixmap = new Pixmap(Gdx.files.internal(data.imagePaths[0]));

	private static final ModelBuilder builder = new ModelBuilder();
	private static final Model SEA, ISLAND, GRAIN, ORE, WOOL, LUMBER, BRICK, GENERIC, ROAD, SETTLEMENT, CITY;

	static
	{
		final ModelBuilder builder = new ModelBuilder();
		SEA = builder.createCylinder(150f, 0f, 150f, 6, WATER, DEFAULT_ATTRS);
		ISLAND = builder.createCylinder(11f, 1f, 11f, 6, DIRT, DEFAULT_ATTRS);
		GRAIN = SettlersOfCatan.assets.getModel("grain.g3db");
		ORE = SettlersOfCatan.assets.getModel("ore.g3db");
		WOOL = SettlersOfCatan.assets.getModel("wool.g3db");
		LUMBER = SettlersOfCatan.assets.getModel("Lumber.g3db");
		GENERIC = SettlersOfCatan.assets.getModel("Desert.g3db");
        BRICK = SettlersOfCatan.assets.getModel("Mine2.g3db");
        ROAD = SettlersOfCatan.assets.getModel("road.g3db");
        SETTLEMENT = SettlersOfCatan.assets.getModel("settlement.g3db");
        CITY = SettlersOfCatan.assets.getModel("city.g3db");
	}

	static ModelInstance getSeaInstance()
	{
		return new ModelInstance(SEA, ORIGIN);
	}

	static ModelInstance getIslandInstance()
	{
		return new ModelInstance(ISLAND, ORIGIN);
	}

	static ModelInstance getHexInstance(final Hex hex)
	{
		final Model model;
		switch (hex.getResource())
		{
		case Brick:   model = BRICK;   break;
		case Generic: model = GENERIC; break;
		case Grain:   model = GRAIN;   break;
		case Lumber:  model = LUMBER;  break;
		case Ore:     model = ORE;     break;
		case Wool:    model = WOOL;    break;
		default:      return null;
		}

		final ModelInstance mod = new ModelInstance(model, hex.get3DPos());
		mod.transform.translate(0, GROUND_LEVEL ,0);
		return mod;
	}
	
	static ModelInstance getChitInstance(final Hex hex)
	{
		final Model chit = builder.createCylinder(2f, 0f, 2f, 16, getChitMaterial(hex.getChit()), DEFAULT_ATTRS);

		final Vector3 pos = hex.get3DPos();
		pos.y = GROUND_LEVEL + 0.8f;
		final ModelInstance instance = new ModelInstance(chit, pos);
		instance.transform.rotate(0, 1, 0, 180);
		return instance;
	}

    private static Material getChitMaterial(final int n)
    {
        final Pixmap tile = new Pixmap(Gdx.files.internal("textures/chit.png"));

        if (n < 10)
        {
            final BitmapFont.Glyph glyph = data.getGlyph(Character.forDigit(n, 10));
            tile.drawPixmap(fontPixmap, (tile.getWidth() - glyph.width) / 2, (tile.getHeight() - glyph.height) / 2,
                    glyph.srcX, glyph.srcY, glyph.width, glyph.height);
        }
        else
        {
            final BitmapFont.Glyph tens = data.getGlyph('1');
            final BitmapFont.Glyph units = data.getGlyph(Character.forDigit(n - 10, 10));
            tile.drawPixmap(fontPixmap, (tile.getWidth() - tens.width - units.width) / 2,
                    (tile.getHeight() - tens.height) / 2, tens.srcX, tens.srcY, tens.width, tens.height);
            tile.drawPixmap(fontPixmap, (tile.getWidth() - units.width + tens.width) / 2,
                    (tile.getHeight() - units.height) / 2, units.srcX, units.srcY, units.width, units.height);
        }

        final Texture texture = new Texture(tile);
        final Material material = new Material(TextureAttribute.createDiffuse(texture));
        final BlendingAttribute blending = new BlendingAttribute();
        blending.opacity = 0.3f;
        material.set(blending);
        return material;
    }

    static ModelInstance getBuildingInstance(final Building building)
    {
        final Model model;
        if (building instanceof Settlement)
            model = SETTLEMENT;
        else if (building instanceof City)
            model = CITY;
        else
            return null;

        final ModelInstance instance = new ModelInstance(model, building.getNode().get3DPos());
        paint(instance, playerToColor(building.getPlayerColour()));
        instance.transform.scale(0.3f, 0.25f, 0.25f);
        instance.transform.translate(0, 1.5f, 0);
        return instance;
    }

    static ModelInstance getRoadInstance(final Road road)
    {

    }

    private static Color playerToColor(final Colour player)
    {
        switch (player)
        {
            case BLUE:   return Color.BLUE;
            case RED:    return Color.RED;
            case WHITE:  return Color.WHITE;
            case ORANGE: return Color.ORANGE;
            default:     return null; // if player == null
        }
    }

    private static void paint(final ModelInstance instance, final Color color)
    {
        instance.materials.get(0).set(ColorAttribute.createDiffuse(color));
    }
}
