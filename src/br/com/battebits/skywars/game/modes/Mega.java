package br.com.battebits.skywars.game.modes;

import br.com.battebits.skywars.game.GameType;

public class Mega extends Team
{
	/*private static final int MAX_PER_ISLAND = 5;*/
	
	public Mega()
	{
		super(GameType.MEGA, 5);
	}
	
	/*@Override
	public void start()
	{
		int i = 1;
		
		Iterator<Player> iterator = playerMap.keySet().iterator();
		
		while (iterator.hasNext())
		{
			if (i > (Bukkit.getMaxPlayers() / MAX_PER_ISLAND))
			{
				Player player = iterator.next();

				player.getInventory().clear();
				player.getInventory().setArmorContents(new ItemStack[4]);
				player.updateInventory();
			}
			else
			{
				Player[] players = new Player[MAX_PER_ISLAND];
		
				for (int k = 0; k < players.length; k++)
					if (iterator.hasNext())
						players[k] = iterator.next();
		
				for (Player player : players)
				{
					if (player != null)
					{
						player.getInventory().clear();
						player.getInventory().setArmorContents(new ItemStack[4]);
						player.updateInventory();
						playerMap.put(player, i);
					}
				}
				
				i++;
			}
		}
	}*/
}
