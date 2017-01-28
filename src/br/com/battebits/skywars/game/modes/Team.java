package br.com.battebits.skywars.game.modes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import br.com.battebits.skywars.game.Engine;
import br.com.battebits.skywars.game.GameStage;
import br.com.battebits.skywars.game.GameType;

public class Team extends Engine
{
	private int maxPerIsland = 2;
	protected Map<Player, Integer> playerMap = new HashMap<>();
	
	public Team()
	{
		super(GameType.TEAM);
	}
	
	public Team(GameType type, int maxPerIsland)
	{
		super(type);
		this.maxPerIsland = maxPerIsland;
	}
	
	@Override
	public void start()
	{
		int i = 1;

		Iterator<Player> iterator = orderPlayers().iterator();
		
		while (iterator.hasNext())
		{
			if (i > (Bukkit.getMaxPlayers() / maxPerIsland))
			{
				Player player = iterator.next();
				
				player.getInventory().clear();
				player.getInventory().setArmorContents(new ItemStack[4]);
			}
			else
			{
				Player[] players = new Player[maxPerIsland];
			
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
		
		setStage(GameStage.PREPARING);
		getSchedule().setTime(10);

		setStarted(System.currentTimeMillis());
	}
	
	@Override
	public void end()
	{
		for (Player player : Bukkit.getOnlinePlayers())
		{
			if (playerMap.containsKey(player))
			{
				
			}
		}
	}
	
	@Override
	public void checkCount()
	{
		Map<Integer, Long> result = playerMap.values().stream().collect(Collectors.groupingBy(v -> v, Collectors.counting()));
		
		if (result.isEmpty())
		{
			Bukkit.shutdown();
		}
		else if (result.size() == 1)
		{
			end();
		}
	}

	@Override
	public Set<Player> getPlayers()
	{
		return playerMap.keySet();
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
	
	private List<Player> orderPlayers()
	{
		List<Player> organized = new ArrayList<>();
		
		// TODO: /party with Redis
		
		for (Player player : playerMap.keySet())
		{
			organized.add(player);
		}
				
		return organized;
	}
}
