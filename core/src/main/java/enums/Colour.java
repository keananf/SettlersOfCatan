package enums;

import protocol.EnumProtos.ColourProto;

public enum Colour
{
	BLUE, RED, ORANGE, WHITE;

	/**
	 * Translate from the protobuf enum to an internally used enum
	 * 
	 * @param colourToTakeFrom
	 * @return
	 */
	public static Colour fromProto(ColourProto colourToTakeFrom)
	{
		switch (colourToTakeFrom)
		{
		case BLUE:
			return Colour.BLUE;
		case RED:
			return Colour.RED;
		case ORANGE:
			return Colour.ORANGE;
		case WHITE:
			return Colour.WHITE;

		}

		return Colour.BLUE;
	}

	/**
	 * Translates the colour value into an enum which is compatible with
	 * protobufs
	 * 
	 * @param col the colour to translate
	 * @return the protobuf compatible enum
	 */
	public static ColourProto toProto(Colour col)
	{
		ColourProto p = ColourProto.BLUE;

		switch (col)
		{
		case BLUE:
			p = ColourProto.BLUE;
			break;
		case RED:
			p = ColourProto.RED;
			break;
		case ORANGE:
			p = ColourProto.ORANGE;
			break;
		case WHITE:
			p = ColourProto.WHITE;
			break;
		}

		return p;
	}

}
