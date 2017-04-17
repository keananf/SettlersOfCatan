package catan.ui;

import catan.SettlersOfCatan;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener;

class SaneTextField extends TextField
{
	SaneTextField(final String placeholder) {
		super(placeholder, SettlersOfCatan.getSkin());

		// background
		final Pixmap backgroundColor = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
		backgroundColor.setColor(1, 1, 1, 0.5f);
		backgroundColor.fill();
		getStyle().background = new Image(new Texture(backgroundColor)).getDrawable();

		// cursor
		final Pixmap cursorColor = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
		cursorColor.setColor(Color.BLACK);
		cursorColor.fill();
		getStyle().cursor = new Image(new Texture(cursorColor)).getDrawable();

		// placeholder text
		addListener(new FocusListener() {
			@Override
			public void keyboardFocusChanged(final FocusEvent event, final Actor actor, final boolean focused) {
				super.keyboardFocusChanged(event, actor, focused);
				if (focused) {
					setText("");
				} else if (((TextField) actor).getText().isEmpty()) {
					((TextField) actor).setText(placeholder);
				}
			}
		});
	}
}
