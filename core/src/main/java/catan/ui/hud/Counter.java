package catan.ui.hud;

import catan.ui.AssMan;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;

import java.util.function.Supplier;

class Counter<T> extends Stack
{
	private final static String TEXTURE_PATH_FMT = "icons/%s.jpg";
	private static Skin skin;
	private static AssMan assets;

	private final T type;
	private final Supplier<Integer> countSupplier;
	private final Label countLabel;

	Counter(final T type, final Supplier<Integer> countSupplier)
	{
		this.type = type;
		this.countSupplier = countSupplier;

		Image backgroundImage = new Image(
				assets.getTexture(
						String.format(TEXTURE_PATH_FMT, type.toString().toLowerCase())));
		add(backgroundImage);

		countLabel = new Label("0", skin);
		add(countLabel);
	}

	T getType()
	{
		return type;
	}

	@Override
	public void act(final float delta)
	{
		Gdx.app.debug("Counter", "Acting");
		final Integer newCount = countSupplier.get();
		countLabel.setText(String.format("%d", newCount));
	}

	static void setup(final Skin skin, final AssMan assets)
	{
		Counter.skin = skin;
		Counter.assets = assets;
	}
}
