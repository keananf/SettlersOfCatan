package catan.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public final class AssetMan extends AssetManager
{
	private static final String MODELS = "models/";
	private static final String TEXTURES = "textures/";
	private static final String BUTTONS = "buttons/";

	private static final HashMap<String, Texture> textures = new HashMap<>();

	Model getModel(final String name)
	{
		final String path = MODELS + name;

		if (!isLoaded(path))
		{
			load(path, Model.class);
			finishLoadingAsset(path);
		}

		return get(path, Model.class);
	}

	public static Texture getTexture(final String name)
	{

		if (textures.containsKey(name))
		{
			return textures.get(name);
		}
		else
		{
			final String path = TEXTURES + name;
			final Texture texture = new Texture(Gdx.files.internal(path));
			textures.put(name, texture);
			return texture;
		}
	}

	static Image getImage(final String name)
	{
		return new Image(getTexture(name));
	}

	public static ImageButton getImageButton(final String name)
	{
		return new ImageButton(getDrawable(BUTTONS + name));
	}

	public static ImageButton getButton(final String name, final Runnable listener)
	{
		final ImageButton btn = new ImageButton(getDrawable(BUTTONS + name));
		btn.addListener(new ClickListener()
		{
			@Override
			public void clicked(final InputEvent event, final float x, final float y)
			{
				listener.run();
			}
		});
		return btn;
	}

	public static TextureRegionDrawable getDrawable(final String name)
	{
		return new TextureRegionDrawable(new TextureRegion(getTexture(name)));
	}
}
