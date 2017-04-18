package catan.ui.hud;

import catan.SettlersOfCatan;
import catan.ui.NumberField;
import catan.ui.SaneTextField;
import client.Client;
import client.Turn;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import enums.ResourceType;
import intergroup.Requests;
import intergroup.board.Board;
import intergroup.trade.Trade;

import java.util.HashMap;
import java.util.Map;

class TradeDialog extends SaneDialog
{
	private final Map<ResourceType, Integer> wanting = new HashMap<>();
	private final Map<ResourceType, Integer> offering = new HashMap<>();

	TradeDialog(Board.Player player, Client client) {
		super("Resources");

		// Add headers
		getContentTable().add(new Label("Resource", SettlersOfCatan.getSkin(), "dialog"));
		getContentTable().add(new Label("Offering", SettlersOfCatan.getSkin(), "dialog"));
		getContentTable().add(new Label("Wanting", SettlersOfCatan.getSkin(), "dialog"));
		getContentTable().row();

		for (ResourceType r : ResourceType.values()) {
			if (r.equals(ResourceType.Generic)) continue;

			getContentTable().add(new Label(r.name(), SettlersOfCatan.getSkin(), "dialog"));
			getContentTable().add(new NumberField("0"));
			getContentTable().add(new NumberField("0"));
			getContentTable().row();
			wanting.put(r, 0);
			offering.put(r, 0);
		}

		addButton("Submit", () -> {
					// If a player trade
					if (player != null) {
						// Set up trade
						Turn turn = new Turn(Requests.Request.BodyCase.INITIATETRADE);
						Trade.WithPlayer.Builder builder = Trade.WithPlayer.newBuilder();
						builder.setOther(player).build();
						builder.setOffering(client.getState().processResources(offering));
						builder.setWanting(client.getState().processResources(wanting));

						// Set Trade
						turn.setPlayerTrade(builder.build());
						client.acquireLocksAndSendTurn(turn);
					} else {
						// Set up trade
						Turn turn = new Turn(Requests.Request.BodyCase.INITIATETRADE);
						Trade.WithBank.Builder builder = Trade.WithBank.newBuilder();
						builder.setOffering(client.getState().processResources(offering));
						builder.setWanting(client.getState().processResources(wanting));

						// Set Trade
						turn.setBankTrade(builder.build());
						client.acquireLocksAndSendTurn(turn);
					}
				}
		);

		addButton("Cancel");
	}
}
