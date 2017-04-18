package catan.ui;

import catan.SettlersOfCatan;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Label;

public class SplashScreen extends MenuScreen
{
	private final SettlersOfCatan game;

	public SplashScreen(final SettlersOfCatan game)
	{
		super("Settlers of Catan");
		this.game = game;

		Label prompt = new Label("Click to start", SettlersOfCatan.getSkin());
		addPrimary(prompt);
	}

	@Override
	public void render(float delta)
	{
		super.render(delta);

		if (Gdx.input.justTouched())
		{
			game.setScreen(new MainMenuScreen(game));
		}
	}
}
