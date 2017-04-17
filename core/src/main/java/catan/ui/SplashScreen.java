package catan.ui;

import catan.SettlersOfCatan;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Label;

public class SplashScreen extends MenuScreen
{
	private final SettlersOfCatan game;

	public SplashScreen(final SettlersOfCatan game)
	{
		super();
		this.game = game;

		Label title = new Label("Settlers of Catan", SettlersOfCatan.getSkin(), "title");
		body.addActor(title);

		Label prompt = new Label("Click to start", SettlersOfCatan.getSkin());
		body.addActor(prompt);

		body.setSize(body.getPrefWidth(), body.getPrefHeight());
	}

	@Override
	public void render(float delta) {
		super.render(delta);

		if (Gdx.input.justTouched())
		{
			game.setScreen(new MainMenuScreen(game));
		}
	}
}
