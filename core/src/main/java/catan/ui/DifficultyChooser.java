package catan.ui;

import catan.SettlersOfCatan;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import enums.Difficulty;

import java.awt.*;
import java.util.HashMap;
import java.util.function.BooleanSupplier;

class DifficultyChooser extends ButtonGroup<CheckBox>
{
	private final BooleanSupplier enabled;
	private final HorizontalGroup group = new HorizontalGroup();
	private final HashMap<CheckBox, Difficulty> checkboxes = new HashMap<>(2);

	DifficultyChooser(final BooleanSupplier enabled)
	{
		this.enabled = enabled;
		group.space(20);

		checkboxes.put(new CheckBox("Easy", SettlersOfCatan.getSkin()), Difficulty.VERYEASY);
		checkboxes.put(new CheckBox("Hard", SettlersOfCatan.getSkin()), Difficulty.EASY);

		for (CheckBox cb : checkboxes.keySet())
		{
			group.addActor(cb);
			add(cb);
		}
	}

	@Override
	protected boolean canCheck(final CheckBox button, final boolean newState)
	{
		final boolean superRes = super.canCheck(button, newState);
		return superRes && enabled.getAsBoolean();
	}

	Difficulty getChosen()
	{
		return checkboxes.get(getChecked());
	}

	HorizontalGroup getGroup()
	{
		return group;
	}
}
