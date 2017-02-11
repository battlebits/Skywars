package br.com.battlebits.skywars.game.modes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import br.com.battlebits.commons.bukkit.BukkitMain;
import br.com.battlebits.skywars.Main;
import br.com.battlebits.skywars.data.PlayerData;
import br.com.battlebits.skywars.game.Engine;
import br.com.battlebits.skywars.game.GameStage;
import br.com.battlebits.skywars.game.GameType;
import br.com.battlebits.skywars.game.task.CageTask;

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

		Iterator<Player> iterator = orderPlayers(maxPerIsland).iterator();
		
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
				
				addCage(new CageTask(this, getMap().getSpawn("is-" + i), players));				

				for (Player player : players)
				{
					if (player != null)
					{
						player.getInventory().clear();
						player.getInventory().setArmorContents(new ItemStack[4]);
						player.teleport(getMap().getSpawn("is-" + i));
						player.updateInventory();
						playerMap.put(player, i);
					}
				}

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
		for (Player player : Bukkit.getOnlinePlayers())
		{
			if (playerMap.containsKey(player))
			{
				PlayerData data = Main.getInstance().getPlayerManager().get(player);
				
				if (data != null)
				{
					data.addWin();
					data.update();
				}
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
	
	private List<Player> orderPlayers(int max)
	{
		List<Player> organized = new ArrayList<>();
	
		/* TODO: Replace to party list */
		for (int i = 0; i < 1; i++)
		{
			Player owner = getPartyOwner();
			
			if (owner != null && contains(owner) && !organized.contains(owner))
			{
				Iterator<Player> i1 = getPartyMembers().iterator();
				Iterator<Player> i2 = playerMap.keySet().iterator();
				
				organized.add(owner);
				
				int toAdd = (max - 1);
				
				while (toAdd > 0)
				{
					if (i1.hasNext())
					{
						Player next = i1.next();
						
						if (contains(next) && !organized.contains(next))
						{
							organized.add(next);
						}
					}
					else if (i2.hasNext())
					{
						Player next = i2.next();
						
						if (!organized.contains(next))
						{
							organized.add(next);
						}
					}
					else
					{
						break;
					}
				}
			}
		}
		
		for (Player player : playerMap.keySet())
		{
			if (!organized.contains(player))
			{
				organized.add(player);
			}
		}
				
		return organized;
	}
	
	private Player getPartyOwner()
	{
		throw new UnsupportedOperationException("Operação não suportada.");
	}
	
	private List<Player> getPartyMembers()
	{
		throw new UnsupportedOperationException("Operação não suportada.");
	}
}
