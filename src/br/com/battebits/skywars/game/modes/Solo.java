package br.com.battebits.skywars.game.modes;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Iterables;

import br.com.battebits.skywars.game.Engine;
import br.com.battebits.skywars.game.GameStage;
import br.com.battebits.skywars.game.GameType;

public class Solo extends Engine
{
	private Map<Player, Integer> playerMap = new HashMap<>();
	
	public Solo()
	{
		super(GameType.SOLO);
	}
	
	@Override
	public void start()
	{
		int i = -1;
		
		Iterator<Player> iterator = playerMap.keySet().iterator();
		
		while (iterator.hasNext())
		{
			Player player = iterator.next();
			
			player.getInventory().clear();
			player.getInventory().setArmorContents(new ItemStack[4]);
			player.updateInventory();
			
			if (i > Bukkit.getMaxPlayers())
			{
				
			}
			else
			{
				
				
				i++;
			}
		}
		
		setStarted(System.currentTimeMillis());
	}
	
	@Override
	public void end()
	{
		if (getStage() != GameStage.ENDING)
		{
			setStage(GameStage.ENDING);
			
			Player winner = Iterables.getFirst(playerMap.keySet(), null);
			
			for (Player other : Bukkit.getOnlinePlayers())
			{
				if (!other.equals(winner))
				{
					// send title etc..
				}
			}
		}
	}
	
	@Override
	public void checkCount()
	{
		if (playerMap.isEmpty())
		{
			Bukkit.shutdown();
		}
		else if (playerMap.size() == 1)
		{
			end();
		}
	}
	
	@Override
	public boolean contains(Player player)
	{
		return playerMap.containsKey(player);
	}
	
	@Override
	public int getIsland(Player player)
	{
		return Optional.ofNullable(playerMap.get(player)).orElse(-1);
	}
	
	@Override
	public void addPlayer(Player player)
	{
		playerMap.put(player, -1);
	}
	
	@Override
	public void removePlayer(Player player)
	{
		playerMap.remove(player);
	}

	@Override
	public Set<Player> getPlayers()
	{
		return playerMap.keySet();
	}
}
