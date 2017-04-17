package catan.ui;

import catan.SettlersOfCatan;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

public class MainMenuScreen extends MenuScreen
{

	MainMenuScreen(final SettlersOfCatan game)
	{
		// Title
		body.addActor(new Label("Settlers of Catan", SettlersOfCatan.getSkin(), "title"));

		// Buttons
		{
			final TextButton button = new TextButton("Start Game", SettlersOfCatan.getSkin());
			button.addListener(new ChangeListener()
					{
				public void changed(ChangeEvent event, Actor actor){
					game.setScreen(new GameScreenSettings(game));
				}
					});
			body.addActor(button);
		}
	
		{
			final TextButton button = new TextButton("Join Remote Game", SettlersOfCatan.getSkin());
			button.addListener(new ChangeListener()
			{
				public void changed(ChangeEvent event, Actor actor)
				{
					Gdx.app.debug("UI", "Button click: Join Remote Game");

					game.setScreen(new RemoteGameScreen(game));
				}
			});
			body.addActor(button);
		}

		{
			final TextButton button = new TextButton("Quit", SettlersOfCatan.getSkin());
			button.addListener(new ChangeListener()
			{
				public void changed(ChangeEvent event, Actor actor)
				{
					Gdx.app.exit();
				}
			});
			body.addActor(button);
		}
	}
}
