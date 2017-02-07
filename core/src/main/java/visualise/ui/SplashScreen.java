package visualise.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import visualise.SettlersOfCatan;

public class SplashScreen implements Screen
{
	final private SettlersOfCatan game;
	final private Stage ui = new Stage(new ScreenViewport());

	public SplashScreen(final SettlersOfCatan game)
	{
		this.game = game;

		Gdx.input.setInputProcessor(ui);

		Image background = new Image(new Texture(Gdx.files.internal("splash.jpg")));
		background.setScaling(Scaling.fill);
		background.setFillParent(true);
		ui.addActor(background);

		VerticalGroup body = new VerticalGroup();
		body.setFillParent(true);
		body.padTop(100);
		body.space(50);
		ui.addActor(body);

		Label title = new Label("Settlers of Catan", game.skin, "title");
		body.addActor(title);

		Label prompt = new Label("Click to start", game.skin);
		body.addActor(prompt);

		body.setSize(body.getPrefWidth(), body.getPrefHeight());
	}

	@Override
	public void render(final float delta)
	{
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		ui.act(delta);
		ui.draw();

		if (Gdx.input.justTouched()) {
			game.setScreen(new MainMenuScreen(game));
		}
	}

	@Override
	public void resize(final int width, final int height)
	{
		ui.getViewport().update(width, height, true);
	}

	@Override
	public void dispose()
	{
		ui.dispose();
	}

	@Override public void pause() {}
	@Override public void resume() {}
	@Override public void hide() {}
	@Override public void show() {}
}
