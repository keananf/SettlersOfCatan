package catan.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import java.util.function.Consumer;

public class IntegerField extends SaneTextField
{
	public IntegerField(final String placeholder, final Consumer<Integer> listener)
	{
		this(placeholder);
		addListener(new ChangeListener()
		{
			@Override
			public void changed(ChangeEvent event, Actor actor)
			{
				listener.accept(getNumericValue());
			}
		});
	}

	IntegerField(final String placeholder)
	{
		super(placeholder);
		setTextFieldFilter(new TextFieldFilter.DigitsOnlyFilter());
	}

	int getNumericValue() throws NumberFormatException
	{
		return Integer.parseInt(getText());
	}
}
