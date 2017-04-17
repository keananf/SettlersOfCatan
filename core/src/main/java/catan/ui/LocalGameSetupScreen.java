package catan.ui;

import AI.LocalAIClient;
import catan.SettlersOfCatan;
import client.Client;
import client.LocalClient;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

class LocalGameSetupScreen extends GameSetupScreen
{
	LocalGameSetupScreen(final SettlersOfCatan game)
	{
		super("Start new game", game);

		final Label numberOfAIsLabel = new Label("Number of AI opponents", SettlersOfCatan.getSkin());
		addPrimary(numberOfAIsLabel);
		final NumberField numberOfAIs = new NumberField("3");
		addPrimary(numberOfAIs);

		setSubmitListener(new ChangeListener()
		{
			@Override
			public void changed(final ChangeEvent event, final Actor actor)
			{
				final Client client;
				if (playerIsAI())
				{
					client = new LocalAIClient(getChosenDifficulty(), getChosenDifficulty(), game, getUsername(), numberOfAIs.getNumericValue());
				}
				else
				{
					client = new LocalClient(game, getChosenDifficulty(), getUsername(), numberOfAIs.getNumericValue());
				}
				game.startNewServer(client);
				game.setScreen(new GameScreen(game));
			}
		});
	}
}