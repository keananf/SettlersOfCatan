package catan.ui;

import catan.SettlersOfCatan;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import enums.Difficulty;

import java.util.function.BooleanSupplier;

public class DifficultyChooser extends ButtonGroup<CheckBox>
{
	private final BooleanSupplier enabled;
	private final HorizontalGroup group = new HorizontalGroup();


	DifficultyChooser(final BooleanSupplier enabled)
	{
		this.enabled = enabled;
		group.space(20);

		final CheckBox[] options = {
				new CheckBox("Easy", SettlersOfCatan.getSkin()),
				new CheckBox("Hard", SettlersOfCatan.getSkin())
		};

		for (CheckBox cb : options)
		{
			group.addActor(cb);
			add(cb);
		}

	}

	@Override
	protected boolean canCheck(CheckBox button, boolean newState) {
		final boolean superRes = super.canCheck(button, newState);
		return superRes && enabled.getAsBoolean();
	}

	Difficulty getChosen()
	{
		switch (getChecked().getText().toString())
		{
			case "Easy": return Difficulty.VERYEASY;
			case "Hard": return Difficulty.EASY;
			default:     return null;
		}
	}

	HorizontalGroup getGroup()
	{
		return group;
	}
}
