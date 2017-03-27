package catan.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Model;

import java.util.HashMap;

public final class AssMan extends AssetManager
{
	private static final String MODELS = "models/";
    private static final String TEXTURES = "textures/";

    private final HashMap<String, Texture> textures = new HashMap<>();

	public AssMan()
	{
		super();
	}

	public Model getModel(String name)
	{
		final String path = MODELS + name;

		if (!isLoaded(path))
		{
			load(path, Model.class);
			finishLoadingAsset(path);
		}

		return get(path, Model.class);
	}

	public Texture getTexture(String name)
    {

        if (textures.containsKey(name))
        {
            return textures.get(name);
        }
        else {
            final String path = TEXTURES + name;
            final Texture texture = new Texture(Gdx.files.internal(path));
            textures.put(name, texture);
            return texture;
        }
    }
}
