package catan.ui;

import catan.SettlersOfCatan;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import enums.Difficulty;

class GameSetupScreen extends MenuScreen
{
	private final SaneTextField nameInput = new SaneTextField("Name");
	private final CheckBox playAsAIInput = new CheckBox("Play as AI", SettlersOfCatan.getSkin());
	private final DifficultyChooser aiChooser = new DifficultyChooser(playAsAIInput::isChecked);

	private final TextButton startBtn = new TextButton("Start", SettlersOfCatan.getSkin());

	GameSetupScreen(final String title, final SettlersOfCatan game)
	{
		super(title);
		// username field
		addPrimary(nameInput);

		// play as AI checkboxes
		playAsAIInput.addListener(new ChangeListener()
		{
			@Override
			public void changed(ChangeEvent event, Actor actor)
			{
				if (((CheckBox) actor).isChecked())
				{
					aiChooser.getGroup().setVisible(true);
				}
				else
				{
					aiChooser.getGroup().setVisible(false);
				}
			}
		});

		// choose AI difficulty
		addPrimary(playAsAIInput);
		aiChooser.getGroup().setVisible(false);
		addPrimary(aiChooser.getGroup());

		final TextButton backBtn = new TextButton("Main Menu", SettlersOfCatan.getSkin());
		backBtn.addListener(new ChangeListener()
		{
			public void changed(final ChangeEvent event, final Actor actor)
			{
				game.setScreen(new MainMenuScreen(game));
			}
		});
		addSecondary(backBtn);
		addSecondary(startBtn);
	}

	void setSubmitListener(final ChangeListener listener)
	{
		startBtn.addListener(listener);
	}

	String getUsername()
	{
		return nameInput.getText();
	}

	boolean playerIsAI()
	{
		return playAsAIInput.isChecked();
	}

	Difficulty getChosenDifficulty()
	{
		return aiChooser.getChosen();
	}
}
