package catan.ui.hud;

import catan.SettlersOfCatan;
import catan.ui.AssetMan;
import client.Client;
import client.Turn;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import game.players.Player;
import intergroup.Requests;
import intergroup.board.Board;

class PlayerBar extends Stack
{
	final Client client;
	private final HeadsUpDisplay hud;

	PlayerBar(final Player player, final Client client, HeadsUpDisplay hud)
	{
		this.client = client;
		this.hud = hud;
		final Image bground = new Image(AssetMan.getTexture("playerbar.png"));
		addActor(bground);
		final HorizontalGroup row = new HorizontalGroup();
		addActor(row);

		final Label name = new Label(player.getUsername(), SettlersOfCatan.getSkin(), "username");
		row.addActor(name);

		// Steal button
		final ImageButton steal = new ImageButton(AssetMan.getDrawable("StealButton.png"));
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
		final ImageButton trade = new ImageButton(AssetMan.getDrawable("Trade2.png"));
		row.addActor(trade);
		trade.addListener(new ClickListener()
		{
			@Override
			public void clicked(InputEvent event, float x, float y)
			{
				super.clicked(event, x, y);
				client.log("UI", String.format("Trade Button with %s Clicked", player.getId().name()));

				TradeDialog dialog = new TradeDialog("Resources", SettlersOfCatan.getSkin(),
						Board.Player.newBuilder().setId(player.getId()).build(), client, hud);
				dialog.show(hud);
			}
		});
	}
}
