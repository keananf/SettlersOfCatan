package catan.ui.hud;

import catan.SettlersOfCatan;
import catan.ui.AssMan;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import game.players.Player;

class PlayerBar extends Stack
{
	private final Player player;

	PlayerBar(final Player player)
	{
		this.player = player;

		final Image bground = new Image(AssMan.getTexture("playerbar.png"));
		addActor(bground);
		final HorizontalGroup row = new HorizontalGroup();
		addActor(row);

		final Label name = new Label(player.getUsername(), SettlersOfCatan.skin, "username");
		row.addActor(name);

		final TextButton tradeBtn = new TextButton("Trade", SettlersOfCatan.skin);
		row.addActor(tradeBtn);
	}
}
