package catan.ui.hud;

import catan.SettlersOfCatan;
import catan.ui.AssetMan;
import client.Client;
import client.Turn;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import game.players.Player;
import intergroup.Requests;
import intergroup.board.Board;

class PlayerBar extends Stack
{
	PlayerBar(final Player player, final Client client, HeadsUpDisplay hud, final boolean isAI)
	{

		final Pixmap background = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
		final Color color = player.getColour().displayColor;
		color.a = 0.8f;
		background.setColor(color);
		background.fillRectangle(0, 0, 250, 50);
		addActor(new Image(new Texture(background)));

		final HorizontalGroup row = new HorizontalGroup();
		row.pad(5);
		row.space(10);
		addActor(row);

		// Name and ID
		final Label name = new Label(player.getUsername(), SettlersOfCatan.getSkin(), "username");
		row.addActor(name);

		// Only display buttons if the player is NOT an AI
		if(!isAI) {
			final VerticalGroup btnCol = new VerticalGroup();
			btnCol.space(5);
			row.addActor(btnCol);

			// Steal button
			final ImageButton steal = AssetMan.getImageButton("steal.png");
			btnCol.addActor(steal);
			steal.addListener(new ClickListener() {
				@Override
				public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
					Turn turn = new Turn(Requests.Request.BodyCase.SUBMITTARGETPLAYER);
					turn.setTarget(player.getColour());
					client.acquireLocksAndSendTurn(turn);
				}
			});

			// Trade button
			final ImageButton trade = AssetMan.getImageButton("trade.png");
			btnCol.addActor(trade);
			trade.addListener(new ClickListener() {
				@Override
				public void clicked(InputEvent event, float x, float y) {
					super.clicked(event, x, y);
					TradeDialog dialog = new TradeDialog("Resources", SettlersOfCatan.getSkin(),
							Board.Player.newBuilder().setId(player.getId()).build(), client, hud);
					dialog.show(hud);
				}
			});
		}
	}
}
