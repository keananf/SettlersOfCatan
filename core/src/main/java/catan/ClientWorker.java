public class ClientWorker
{
	private Game game;

	public ClientWorker(Game game)
	{
		this.game = game;
	}

	public void notify()
	{
		//TODO: check turn in progress
		//TODO: call sendMove if turn in progress is full
		//TODO: if action is buying development card call checkCard
		//TODO: If build action check for clicked node or edge
		//TODO: call checkMoves passing in clicked node
	}

	private void checkCard()
	{
		//TODO: check that the player can buy a development card
	}

	private void checkBuild(Node node)
	{
		//TODO: check which build actions the player can take and update game state
	}

	private void checkBuild(Edge edge)
	{
		//TODO: exactly the same as above but for edges
	}

	public sendMove()
	{
		//TODO: create protocol buffer from game state
		//TODO: send protocol buffer to server
	}
}