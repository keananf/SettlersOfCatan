package catan.ui.hud;

import catan.SettlersOfCatan;
import catan.ui.AssMan;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import game.players.Player;

class PlayerBar extends Stack
{

	PlayerBar(final Player player)
	{
		final Image bground = new Image(AssMan.getTexture("playerbar.png"));
		addActor(bground);
		final HorizontalGroup row = new HorizontalGroup();
		addActor(row);

		final Label name = new Label(player.getUsername(), SettlersOfCatan.getSkin(), "username");
		row.addActor(name);

		final TextButton tradeBtn = new TextButton("Trade", SettlersOfCatan.getSkin());
		row.addActor(tradeBtn);
	}
}
