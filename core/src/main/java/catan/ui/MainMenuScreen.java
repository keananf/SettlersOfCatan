package catan.ui;

import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.Game;

import catan.SettlersOfCatan;

public class MainMenuScreen implements Screen
{
	final private SettlersOfCatan game;
	private OrthographicCamera camera;
	private BitmapFont font40;
	private BitmapFont font30;

	public MainMenuScreen(final SettlersOfCatan game)
	{
		this.game = game;

		camera = new OrthographicCamera();
		camera.setToOrtho(false, 800, 480);

		FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("junction.ttf"));
		FreeTypeFontParameter parameter = new FreeTypeFontParameter();
		parameter.size = 40;
		font40 = generator.generateFont(parameter);
		parameter.size = 30;
		font30 = generator.generateFont(parameter);
		generator.dispose();
	}

	@Override
	public void render(float delta)
	{
		Gdx.gl.glClearColor(0, 0, 0.2f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		camera.update();
		game.batch.setProjectionMatrix(camera.combined);

		game.batch.begin();
		font40.draw(game.batch, "Settlers of Catan", 100, 150);
		font30.draw(game.batch, "Click to start", 100, 100);
		game.batch.end();
	}

	@Override public void pause() {}
	@Override public void resume() {}
	@Override public void hide() {}
	@Override public void show() {}
	@Override public void dispose() {}
	@Override public void resize(int width, int height) {}
}
