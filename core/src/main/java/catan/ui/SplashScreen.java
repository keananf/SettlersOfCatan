package catan.ui;

import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.Game;

import catan.SettlersOfCatan;

public class SplashScreen implements Screen
{
	final private SettlersOfCatan game;
	private OrthographicCamera camera;
	private BitmapFont font40;
	private BitmapFont font30;
	private Texture edwin;

	public SplashScreen(final SettlersOfCatan game)
	{
		this.game = game;

		camera = new OrthographicCamera();
		camera.setToOrtho(false, 800, 480);

		FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("SourceSansPro-Regular.ttf"));
		FreeTypeFontParameter parameter = new FreeTypeFontParameter();
		parameter.minFilter = TextureFilter.Linear;
		parameter.magFilter = TextureFilter.Linear;
		parameter.size = 40;
		font40 = generator.generateFont(parameter);
		parameter.size = 30;
		font30 = generator.generateFont(parameter);
		generator.dispose();

		edwin = new Texture(Gdx.files.internal("edwin.png"));
	}

	@Override
	public void render(float delta)
	{
		Gdx.gl.glClearColor(0, 0, 0.2f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		camera.update();
		game.batch.setProjectionMatrix(camera.combined);

		game.batch.begin();
		game.batch.draw(edwin, 350, 10);
		font40.draw(game.batch, "Settlers of Catan", 60, 150);
		font30.draw(game.batch, "Click to start", 60, 100);
		game.batch.end();

		if (Gdx.input.justTouched()) {
			game.setScreen(new MainMenuScreen(game));
		}
	}

	@Override public void dispose()
	{
		font40.dispose();
		font30.dispose();
		edwin.dispose();
	}

	@Override public void pause() {}
	@Override public void resume() {}
	@Override public void hide() {}
	@Override public void show() {}
	@Override public void resize(int width, int height) {}
}
