package catan.ui.hud;

import catan.SettlersOfCatan;
import catan.ui.IntegerField;
import client.Client;
import client.Turn;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import enums.ResourceType;
import intergroup.Requests;

import java.util.HashMap;
import java.util.Map;

class DiscardDialog extends SaneDialog
{
	private final Map<ResourceType, Integer> resources = new HashMap<>();

	DiscardDialog(final Client client)
	{
		super("Discard cards");

		getContentTable().add(new Label("Resource", SettlersOfCatan.getSkin(), "dialog"));
		getContentTable().add(new Label("Quantity", SettlersOfCatan.getSkin(), "dialog"));
		getContentTable().row();

		for (ResourceType r : ResourceType.values())
		{
			if (r.equals(ResourceType.Generic)) continue;
			resources.put(r, 0);
			getContentTable().add(new Label(r.name(), SettlersOfCatan.getSkin(), "dialog"));
			getContentTable().add(new IntegerField("0", (n) -> resources.put(r, n)));
			getContentTable().row();
		}

		addButton("Submit", new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				super.clicked(event, x, y);

				Turn turn = new Turn(Requests.Request.BodyCase.DISCARDRESOURCES);
				turn.setChosenResources(resources);
				client.acquireLocksAndSendTurn(turn);
			}
		});
		addButton("Cancel");
	}
}