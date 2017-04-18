package enums;

import com.badlogic.gdx.graphics.Color;
import intergroup.lobby.Lobby;

public enum Colour
{
	BLUE(Color.BLUE), RED(Color.RED), ORANGE(Color.ORANGE), WHITE(Color.WHITE);

	public final Color displayColor;

	Colour(Color displayColor)
	{
		this.displayColor = displayColor;
	}

	/**
	 * Translate from the protobuf enum to an internally used enum
	 *
	 * @param colourToTakeFrom
	 * @return
	 */
	public static Colour fromProto(Lobby.GameSetup.PlayerSetting.Colour colourToTakeFrom)
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
	public static Lobby.GameSetup.PlayerSetting.Colour toProto(Colour col)
	{
		Lobby.GameSetup.PlayerSetting.Colour p = Lobby.GameSetup.PlayerSetting.Colour.BLUE;

		switch (col)
		{
		case BLUE:
			p = Lobby.GameSetup.PlayerSetting.Colour.BLUE;
			break;
		case RED:
			p = Lobby.GameSetup.PlayerSetting.Colour.RED;
			break;
		case ORANGE:
			p = Lobby.GameSetup.PlayerSetting.Colour.ORANGE;
			break;
		case WHITE:
			p = Lobby.GameSetup.PlayerSetting.Colour.WHITE;
			break;
		}

		return p;
	}
}