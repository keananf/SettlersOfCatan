package catan.ui.hud;

import catan.SettlersOfCatan;
import catan.ui.AssetMan;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.utils.Align;

import java.util.function.Supplier;

import static java.lang.String.format;

class Counter extends Stack
{
	private final static String ICON_FMT = "icons/%s.png";

	private final Supplier<Integer> countSupplier;
	private final Label countLabel;

	Counter(final String type, final Supplier<Integer> countSupplier)
	{
		this(new Image(AssetMan.getTexture(format(ICON_FMT, type))), countSupplier);
	}

	private Counter(final Image bground, final Supplier<Integer> countSupplier)
	{
		this.countSupplier = countSupplier;
		add(bground);

		countLabel = new Label("0", SettlersOfCatan.getSkin());
		countLabel.setAlignment(Align.center);
		add(countLabel);
	}

	@Override
	public void act(final float delta)
	{
		final Integer newCount = countSupplier.get();
		countLabel.setText(format("%d", newCount));
	}
}