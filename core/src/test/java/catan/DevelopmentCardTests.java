package test.java.catan;

import static org.junit.Assert.*;
import main.java.enums.DevelopmentCardType;
import main.java.exceptions.*;
import main.java.game.build.DevelopmentCard;

import org.junit.*;

public class DevelopmentCardTests extends TestHelper
{
	DevelopmentCard c;
	
	@Before
	public void setUp()
	{
		reset();
		c = new DevelopmentCard();
		c.setColour(p.getColour());
		c.setType(DevelopmentCardType.Library);
	}

	@Test(expected = CannotAffordException.class)
	public void cannotBuyDevCardTest() throws CannotAffordException
	{
		p.buyDevelopmentCard();
	}
	
	@Test(expected = DoesNotOwnException.class)
	public void cannotPlayDevCardTest() throws DoesNotOwnException
	{
		// This development card does not exist in the player's hand 
		p.playDevelopmentCard(c);
	}	

	@Test
	public void buyDevelopmentCardTest() throws CannotAffordException
	{
		// Grant resources and buy a card
		assertTrue(p.getDevelopmentCards().size() == 0);
		p.grantResources(DevelopmentCard.getCardCost());
		buyDevelopmentCard();
		assertTrue(p.getDevelopmentCards().size() > 0);
	}
	
	@Test
	public void playAndRemoveDevelopmentCardTest() throws CannotAffordException, DoesNotOwnException
	{
		// Grant resources and buy a card
		assertTrue(p.getDevelopmentCards().size() == 0);
		p.grantResources(DevelopmentCard.getCardCost());
		buyDevelopmentCard();
		assertTrue(p.getDevelopmentCards().size() > 0);
		
		// Play card and test it was removed
		DevelopmentCardType key = (DevelopmentCardType) p.getDevelopmentCards().keySet().toArray()[0];
		p.playDevelopmentCard(p.getDevelopmentCards().get(key).get(0));
	}
}
