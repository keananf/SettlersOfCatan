package catan.ui.hud;

import catan.SettlersOfCatan;
import client.Client;
import client.Turn;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import enums.ResourceType;
import intergroup.Requests;
import intergroup.trade.Trade;

import java.util.Map;

class TradeResponseDialog extends SaneDialog
{

	TradeResponseDialog(Client client)
	{
		super("Trade response");

		final Trade.WithPlayer trade = client.getTurn().getCurrentTrade().getTrade();
		final Map<ResourceType, Integer> offerResources = client.getState().processResources(trade.getOffering());
		final Map<ResourceType, Integer> requestResources = client.getState().processResources(trade.getWanting());

		// Add labels

		getContentTable().add(new Label("Resource", SettlersOfCatan.getSkin(), "dialog"));
		getContentTable().add(new Label("Offering", SettlersOfCatan.getSkin(), "dialog"));
		getContentTable().add(new Label("Wanting", SettlersOfCatan.getSkin(), "dialog"));
		getContentTable().row();

		for (ResourceType r : ResourceType.values())
		{
			if (r.equals(ResourceType.Generic)) continue;

			final String offer = offerResources.getOrDefault(r, 0).toString();
			final String request = requestResources.getOrDefault(r, 0).toString();

			getContentTable().add(new Label(r.name(), SettlersOfCatan.getSkin(), "dialog"));
			getContentTable().add(new Label(offer, SettlersOfCatan.getSkin(), "dialog"));
			getContentTable().add(new Label(request, SettlersOfCatan.getSkin(), "dialog"));
			getContentTable().row();
		}

		addButton("Accept", () -> {
			// Set up trade
			Turn turn = new Turn(Requests.Request.BodyCase.SUBMITTRADERESPONSE);
			turn.setTradeResponse(Trade.Response.ACCEPT);
			turn.setPlayerTrade(client.getTurn().getCurrentTrade().getTrade());

			// Set Trade
			client.acquireLocksAndSendTurn(turn);
		});

		addButton("Reject", () -> {
			// Set up trade
			Turn turn = new Turn(Requests.Request.BodyCase.SUBMITTRADERESPONSE);
			turn.setTradeResponse(Trade.Response.REJECT);
			turn.setPlayerTrade(client.getTurn().getCurrentTrade().getTrade());

			// Set Trade
			client.acquireLocksAndSendTurn(turn);
		});
	}
}