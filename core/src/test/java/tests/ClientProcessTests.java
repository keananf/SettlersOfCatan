package tests;

import board.Hex;
import board.HexGrid;
import board.Node;
import client.ClientGame;
import org.junit.Before;
import org.junit.Test;
import protocol.BoardProtos;

import static org.junit.Assert.assertTrue;

public class ClientProcessTests extends TestHelper
{

    @Before
    public void setUp()
    {
        game = new ClientGame();
    }

    @Test
    public void processBoardTest()
    {
        // Retrieve board and its protobuf representation
        HexGrid actualBoard = game.getGrid();
        BoardProtos.BoardProto board = game.getBoard().getBoard();

        // Simulate processing of protobuf
        HexGrid processedBoard = ((ClientGame) game).setBoard(board);

        // Assert all nodes were serialised and deserialised
        for(Node n1 : actualBoard.getNodesAsList())
        {
            assertTrue(processedBoard.getNodesAsList().contains(n1));
        }

        // Assert all hexes were serialised and deserialised
        for(Hex h1 : actualBoard.getHexesAsList())
        {
            assertTrue(processedBoard.getHexesAsList().contains(h1));
        }
    }
}
