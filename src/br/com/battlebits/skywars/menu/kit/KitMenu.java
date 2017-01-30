package br.com.battlebits.skywars.menu.kit;

import br.com.battlebits.commons.api.menu.MenuInventory;

public class KitMenu extends MenuInventory 
{
	private int[] bounds;
	
	public KitMenu()
	{
		this(new int[] {0, 45});
	}
	
	public KitMenu(int[] bounds)
	{
		super("Kit Selector -> " + (bounds[1] / 45), 6);
		this.bounds = bounds;
		addContents();
	}
	
	public void addContents()
	{
		if (bounds[0] > 0)
		{
			
		}
		
	}
}
