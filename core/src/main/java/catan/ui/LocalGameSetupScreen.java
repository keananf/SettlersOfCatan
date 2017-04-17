package catan.ui;

import AI.LocalAIClient;
import catan.SettlersOfCatan;
import client.Client;
import client.LocalClient;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import enums.Difficulty;

class LocalGameSetupScreen extends GameSetupScreen
{
	LocalGameSetupScreen(final SettlersOfCatan game)
	{
		super("Start new game", game);

		final NumberField numberOfAIs = new NumberField("Number of AI opponents");
		addPrimary(numberOfAIs);

		setSubmitListener(new ChangeListener()
		{
			@Override
			public void changed(final ChangeEvent event, final Actor actor)
			{
				final Client client;
				if (playerIsAI())
				{
					client = new LocalAIClient(getChosenDifficulty(), game, getUsername(), numberOfAIs.getNumericValue());
				}
				else
				{
					client = new LocalClient(game, getUsername(), numberOfAIs.getNumericValue());
				}
				game.startNewServer(client);
				game.setScreen(new GameScreen(game));
			}
		});
	}
}