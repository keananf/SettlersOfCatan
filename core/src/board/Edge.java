package board;


/**
 * Class uniquely describing an edge (between two edges)
 * @author 140001596
 */
public class Edge //TODO extend BoardElement
{
	Node x, y; // way of uniquely describing an edge
	
	public Edge(Node x, Node y)
	{
		this.x = x;
		this.y = y;
	}

}
