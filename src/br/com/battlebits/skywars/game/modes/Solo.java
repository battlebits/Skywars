package br.com.battlebits.skywars.game.modes;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Iterables;

import br.com.battlebits.commons.bukkit.BukkitMain;
import br.com.battlebits.skywars.Main;
import br.com.battlebits.skywars.data.PlayerData;
import br.com.battlebits.skywars.game.Engine;
import br.com.battlebits.skywars.game.GameStage;
import br.com.battlebits.skywars.game.GameType;
import br.com.battlebits.skywars.game.task.CageTask;

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
		int i = 1;
		
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
				playerMap.put(player, i);
				addCage(new CageTask(this, getMap().getSpawn("is-" + i), player));				
				i++;
			}
		}
		
		applyRefill("player-1", "player-1");
		applyRefill("player-2", "player-2");
		applyRefill("player-3", "player-3");
		applyRefill("feast", "feast");

		setStage(GameStage.PREPARING);
		getSchedule().setTime(10);
		
		setStarted(System.currentTimeMillis());
		BukkitMain.getInstance().setTagControl(false);
	}
	
	@Override
	public void end()
	{
		if (getStage() != GameStage.ENDING)
		{
			setStage(GameStage.ENDING);
			
			Player winner = Iterables.getFirst(playerMap.keySet(), null);
			
			PlayerData data = Main.getInstance().getPlayerManager().get(winner);
			
			if (data != null)
			{
				data.addWin();
				data.update();
			}
			
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
		int id = -1;
		if (playerMap.containsKey(player))
			id = playerMap.get(player);
		return id;		
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
