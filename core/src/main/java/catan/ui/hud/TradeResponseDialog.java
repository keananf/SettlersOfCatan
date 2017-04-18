package catan.ui.hud;

import catan.SettlersOfCatan;
import client.Client;
import client.Turn;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import enums.ResourceType;
import intergroup.Requests;
import intergroup.board.Board;
import intergroup.trade.Trade;

import java.util.Map;

public class TradeResponseDialog extends Dialog
{
	private final Board.Player sender;
	private final Client client;
	private final Trade.WithPlayer trade;
	Map<ResourceType, Integer> offerResources, requestResources;

	public TradeResponseDialog(Skin skin, Client client, HeadsUpDisplay hud)
	{
		super("Trade", skin);
		this.sender = client.getTurn().getCurrentTrade().getInstigator();
		this.client = client;
		this.trade = client.getTurn().getCurrentTrade().getTrade();
		offerResources = client.getState().processResources(trade.getOffering());
		requestResources = client.getState().processResources(trade.getWanting());

		VerticalGroup vert = new VerticalGroup();
		final Table root = new Table();
		hud.getResources();
		root.setFillParent(true);
		addActor(root);

		// Add labels
		HorizontalGroup horiz = new HorizontalGroup();
		TextField offering = new TextField("Offer", SettlersOfCatan.getSkin());
		offering.setTextFieldListener((textField, c) -> textField.setText("Offer"));
		TextField wanting = new TextField("Request", SettlersOfCatan.getSkin());
		wanting.setTextFieldListener((textField, c) -> textField.setText("Request"));
		horiz.addActor(offering);
		horiz.addActor(wanting);
		vert.addActor(horiz);

		for (ResourceType r : ResourceType.values())
		{
			if (r.equals(ResourceType.Generic)) continue;

			addTradeInfo(r, vert);
		}

		root.add(vert);
		addConfirmButtons();
	}

	private void addTradeInfo(ResourceType r, VerticalGroup vert)
	{
		HorizontalGroup horiz = new HorizontalGroup();
		int offer = offerResources.getOrDefault(r, 0);
		int request = requestResources.getOrDefault(r, 0);
		String msg = offer > 0 ? String.valueOf(offer) : r.name();
		String msg2 = request > 0 ? String.valueOf(request) : r.name();

		// List trade contents for this resource
		TextField text = new TextField(msg, SettlersOfCatan.getSkin());
		text.setTextFieldListener((textField, c) -> textField.setText(msg));
		TextField text2 = new TextField(msg2, SettlersOfCatan.getSkin());
		text.setTextFieldListener((textField, c) -> textField.setText(msg2));
		horiz.addActor(text);
		horiz.addActor(text2);

		vert.addActor(horiz);
	}

	private void addConfirmButtons()
	{
		TextButton button = new TextButton("Accept", SettlersOfCatan.getSkin());
		button.addListener(new ClickListener()
		{
			@Override
			public void clicked(InputEvent event, float x, float y)
			{
				super.clicked(event, x, y);

				// Set up trade
				Turn turn = new Turn(Requests.Request.BodyCase.SUBMITTRADERESPONSE);
				turn.setTradeResponse(Trade.Response.ACCEPT);
				turn.setPlayerTrade(client.getTurn().getCurrentTrade().getTrade());

				// Set Trade
				client.acquireLocksAndSendTurn(turn);

			}
		});
		TextButton cancel = new TextButton("Reject", SettlersOfCatan.getSkin());
		cancel.addListener(new ClickListener()
		{
			@Override
			public void clicked(InputEvent event, float x, float y)
			{
				super.clicked(event, x, y);

				// Set up trade
				Turn turn = new Turn(Requests.Request.BodyCase.SUBMITTRADERESPONSE);
				turn.setTradeResponse(Trade.Response.REJECT);
				turn.setPlayerTrade(client.getTurn().getCurrentTrade().getTrade());

				// Set Trade
				client.acquireLocksAndSendTurn(turn);
			}
		});

		button(button, true).button(cancel, false);
	}
}