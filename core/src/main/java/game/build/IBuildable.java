package main.java.game.build;

import main.java.enums.ResourceType;

import java.util.Map;

/**
 * Interface that describes things that can be constructed from resources
 * @author 140001596
 */
public interface IBuildable
{
	public Map<ResourceType, Integer> getCost();
}
