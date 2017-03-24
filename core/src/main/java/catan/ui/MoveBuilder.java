package catan.ui;

import java.util.concurrent.Semaphore;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import grid.Node;
import intergroup.Requests;
import client.Client;
import grid.BoardElement;
import grid.Edge;
import grid.Hex;

public class MoveBuilder {
private final Client client;
	public MoveBuilder(Client client)
	{
		this.client = client;	
	}
	
	protected void onSelect(BoardElement element)
	{
		try {
		    Semaphore lock = client.getTurnLock();
		    lock.acquire();
		
		    if(element instanceof Node){
		    	client.getTurn().setChosenNode((Node) element);
		    	client.getTurn().setChosenMove(Requests.Request.BodyCase.BUILDSETTLEMENT);
		    } if(element instanceof Edge){
		    	client.getTurn().setChosenEdge((Edge) element);
		    	client.getTurn().setChosenMove(Requests.Request.BodyCase.BUILDROAD);           	
		    	
		    }if(element instanceof Hex){
		    	client.getTurn().setChosenHex((Hex) element);
		    	client.getTurn().setChosenMove(Requests.Request.BodyCase.MOVEROBBER);
		    }
		
		    client.sendTurn();
		} catch (InterruptedException e) {
		    e.printStackTrace();
		} //TODO   
    }

}
