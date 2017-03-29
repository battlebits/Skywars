package br.com.battlebits.skywars.game.task;

import java.util.HashSet;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import br.com.battlebits.commons.api.title.TitleAPI;
import br.com.battlebits.skywars.game.Engine;
import br.com.battlebits.skywars.game.kits.Kit;

@SuppressWarnings("deprecation")
public class CageTask 
{
	private Player[] players;
	private Location location;
	private HashSet<Block> blocks;
	
	public CageTask(Engine engine, Location location, Player... players)
	{
		this.players = players;
		this.location = location;
		this.blocks = new HashSet<>();
		
		switch (engine.getType()) 
		{
		    case SOLO:
		    {
		    	blocks.add(location.clone().add(0, 0, 1).getBlock());
	            blocks.add(location.clone().add(1, 0, 0).getBlock());
	            blocks.add(location.clone().add(0, 1, 1).getBlock());
	            blocks.add(location.clone().add(1, 1, 0).getBlock());
	            blocks.add(location.clone().add(0, 2, 1).getBlock());
	            blocks.add(location.clone().add(1, 2, 0).getBlock());
	            blocks.add(location.clone().add(0, 3, 0).getBlock());
	            blocks.add(location.clone().add(0, -1, 0).getBlock());
	            blocks.add(location.clone().add(0, 0, -1).getBlock());
	            blocks.add(location.clone().add(-1, 0, 0).getBlock());
	            blocks.add(location.clone().add(0, 1, -1).getBlock());
	            blocks.add(location.clone().add(-1, 1, 0).getBlock());
	            blocks.add(location.clone().add(0, 2, -1).getBlock());
	            blocks.add(location.clone().add(-1, 2, 0).getBlock());
		    	break;
		    }
			
		    default:
		    {
		    	for (int x = -2; x <= 2; ++x)
	            {
		    		for (int z = -2; z <= 2; ++z)
		    		{
		    			for (int y = -2; y <= 2; ++y)
		                {
		    				if ((x % 2 == 0 && x != 0) || (z % 2 == 0 && z != 0) || (y % 2 == 0 && y != 0))
	                        {
	                           blocks.add(location.clone().add(x, y, z).getBlock());
	                        }
		                }
		    		}
	            }
		    	
		    	break;
		    }
		}

        blocks.forEach(block -> block.setType(Material.GLASS));

        for (Player player : players)
		{
			if (player != null && player.isOnline())
			{
				player.teleport(location.add(0D, 1.5D, 0D));
			}
		}
		
		stepEffects();
	}
	
	public void run(int time)
	{
		if (time <= 0)
		{
			stepEffects();

			blocks.forEach(block -> block.setType(Material.AIR));
			
			for (Player player : players)
			{
				if (player != null && player.isOnline())
				{
					Kit kit = Kit.getKit(player);
					
					if (kit != null)
					{
						kit.applyItems(player);
					}
				}
			}
		}
		else if (time <= 5)
		{
			for (Player player : players)
			{
				if (player != null && player.isOnline())
				{
					player.playSound(player.getLocation(), Sound.NOTE_PLING, 1F, 2F);
					TitleAPI.setTitle(player, (time > 3 ? "§e" : "§c") + time, "§%skywars-cage-opening%§", 0, 30, 0, true);					
				}
			}
		}
		
		for (Player player : players)
		{
			if (player != null && player.isOnline())
			{
				if (player.getLocation().distance(location) > 3D)
				{
				    player.teleport(location.clone().add(0D, 1.5D, 0D));
				}
			}
		}
	}
	
	public void stepEffects()
	{
	    blocks.forEach(block -> block.getWorld().spigot().playEffect(block.getLocation(), Effect.STEP_SOUND, block.getTypeId(), block.getData(), 0.0F, 0.0F, 0.0F, 1.0F, 1, 64));
	}
}
