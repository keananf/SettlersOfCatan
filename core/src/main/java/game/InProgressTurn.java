package game;

import enums.Move;
import enums.ClickObject;

public class InProgressTurn 
{
	public ClickObject initialClickObject = null;
	public Move[] possibilities = {null, null};
	public Move chosenMove = null;
}
