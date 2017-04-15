package catan.ui.hud;

import catan.SettlersOfCatan;
import catan.ui.AssMan;
import client.Client;
import client.Turn;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import game.players.Player;
import intergroup.Requests;
import intergroup.board.Board;
import intergroup.trade.Trade;

class PlayerBar extends Stack
{
	final Client client;
	PlayerBar(final Player player, final Client client)
	{
		this.client = client;
		final Image bground = new Image(AssMan.getTexture("playerbar.png"));
		addActor(bground);
		final HorizontalGroup row = new HorizontalGroup();
		addActor(row);

		final Label name = new Label(player.getUsername(), SettlersOfCatan.getSkin(), "username");
		row.addActor(name);

		// Steal button
		final ImageButton steal = new ImageButton(AssMan.getDrawable("Steal.png"));
		row.addActor(steal);
		steal.addListener(new ClickListener()
		{
			@Override
			public void clicked(InputEvent event, float x, float y)
			{
				super.clicked(event, x, y);
				client.log("UI", "Submit Target Player Button Clicked");
				Turn turn = new Turn(Requests.Request.BodyCase.SUBMITTARGETPLAYER);
				turn.setTarget(player.getColour());
				client.acquireLocksAndSendTurn(turn);
			}
		});

		// Trade button
		final ImageButton trade = new ImageButton(AssMan.getDrawable("Trade2.png"));
		row.addActor(trade);
		trade.addListener(new ClickListener()
		{
			@Override
			public void clicked(InputEvent event, float x, float y)
			{
				super.clicked(event, x, y);
				client.log("UI", String.format("Trade Button with %s Clicked", player.getId().name()));

				// Set up trade
				Turn turn = new Turn(Requests.Request.BodyCase.INITIATETRADE);
				Trade.WithPlayer.Builder builder = Trade.WithPlayer.newBuilder();
				builder.setOther(Board.Player.newBuilder().setId(player.getId()).build());

				//TODO SETUP RESOURCES

				// Set Trade
				turn.setPlayerTrade(builder.build());
				//client.acquireLocksAndSendTurn(turn);
			}
		});
	}
}
