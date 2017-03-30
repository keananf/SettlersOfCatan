package catan.ui;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g3d.Model;

public final class AssMan extends AssetManager
{
	private static final String MODELS = "models/";

	AssMan()
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
}