package main.java.game.moves;

public class PlayRoadBuildingCardMove extends Move
{
	private BuildRoadMove move1, move2;

	/**
	 * @return the move1
	 */
	public BuildRoadMove getMove1()
	{
		return move1;
	}

	/**
	 * @param move1 the move1 to set
	 */
	public void setMove1(BuildRoadMove move1)
	{
		this.move1 = move1;
	}

	/**
	 * @return the move2
	 */
	public BuildRoadMove getMove2()
	{
		return move2;
	}

	/**
	 * @param move2 the move2 to set
	 */
	public void setMove2(BuildRoadMove move2)
	{
		this.move2 = move2;
	}
}
