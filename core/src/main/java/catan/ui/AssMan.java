package catan.ui;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g3d.Model;

public final class AssMan extends AssetManager
{
	private static final String MODELS = "models/";

	public AssMan()
	{
		super();
	}

	public Model getModel(String name)
	{
		return get(MODELS + name, Model.class);
	}
}
