package catan.ui;

import catan.SettlersOfCatan;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

public class MainMenuScreen extends MenuScreen
{

	MainMenuScreen(final SettlersOfCatan game)
	{
		super("Main menu");

		// Buttons
		{
			final TextButton button = new TextButton("Start Game", SettlersOfCatan.getSkin());
			button.addListener(new ChangeListener()
			{
				public void changed(ChangeEvent event, Actor actor)
				{
					game.setScreen(new LocalGameSetupScreen(game));
				}
			});
			addPrimary(button);
		}
	
		{
			final TextButton button = new TextButton("Join Remote Game", SettlersOfCatan.getSkin());
			button.addListener(new ChangeListener()
			{
				public void changed(ChangeEvent event, Actor actor)
				{
					game.setScreen(new RemoteGameSetupScreen(game));
				}
			});
			addPrimary(button);
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
			addPrimary(button);
		}
	}
}
