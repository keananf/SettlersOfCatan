package catan.ui.hud;

import catan.SettlersOfCatan;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

abstract class SaneDialog extends Dialog {
	SaneDialog(final String title)
	{
		super(title, SettlersOfCatan.getSkin());
		setModal(true);
		setMovable(false);
		setResizable(false);
		pad(20);
		center();
		getTitleTable().top();
		getTitleLabel().setAlignment(Align.center);
	}

	void addButton(final String label)
	{
		button(new TextButton(label, SettlersOfCatan.getSkin(), "dialog"));
	}

	void addButton(final String label, final Runnable clickListener)
	{
		final TextButton button = new TextButton(label, SettlersOfCatan.getSkin(), "dialog");
		button.addListener(new ClickListener()
		{
			@Override
			public void clicked(InputEvent event, float x, float y) {
				clickListener.run();
			}
		});
		button(button);
	}
}
