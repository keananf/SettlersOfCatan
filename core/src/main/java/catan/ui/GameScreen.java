package catan.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import catan.SettlersOfCatan;

public class GameScreen implements Screen
{
	final private SettlersOfCatan game;
	final private Stage ui = new Stage(new ScreenViewport());

	public GameScreen(final SettlersOfCatan game)
	{
		this.game = game;
		Gdx.input.setInputProcessor(ui);
	}

	@Override
	public void render(final float delta)
	{
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		ui.act(delta);
		ui.draw();
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

	// Required but unused
	@Override public void pause() {}
	@Override public void resume() {}
	@Override public void hide() {}
	@Override public void show() {}
}
