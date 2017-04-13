package game.build;

import enums.ResourceType;

import java.util.Map;

/**
 * Interface that describes things that can be constructed from resources
 * 
 * @author 140001596
 */
public interface IBuildable
{
	Map<ResourceType, Integer> getCost();
}
