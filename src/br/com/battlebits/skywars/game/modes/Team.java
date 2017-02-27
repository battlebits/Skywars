package br.com.battlebits.skywars.game.modes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import br.com.battlebits.commons.BattlebitsAPI;
import br.com.battlebits.commons.api.title.TitleAPI;
import br.com.battlebits.commons.bukkit.BukkitMain;
import br.com.battlebits.commons.bukkit.party.BukkitParty;
import br.com.battlebits.commons.core.account.BattlePlayer;
import br.com.battlebits.commons.core.party.Party;
import br.com.battlebits.commons.core.translate.Language;
import br.com.battlebits.commons.core.translate.T;
import br.com.battlebits.skywars.Main;
import br.com.battlebits.skywars.data.PlayerData;
import br.com.battlebits.skywars.game.Engine;
import br.com.battlebits.skywars.game.GameStage;
import br.com.battlebits.skywars.game.GameType;
import br.com.battlebits.skywars.game.task.CageTask;
import br.com.battlebits.skywars.utils.Utils;

public class Team extends Engine
{
	private int maxPerIsland = 2;
	protected Map<Player, Integer> playerMap = new HashMap<>();
	protected Map<Player, Integer> islandMap = new HashMap<>();
	
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
		Iterator<Player> iterator = orderPlayers(maxPerIsland).iterator();
		
		for (int i = 1; iterator.hasNext(); i++)
		{
			if (i > (Bukkit.getMaxPlayers() / maxPerIsland))
			{
				Player player = iterator.next();
				
				Utils.clearInventory(player);
				Utils.addSpectatorItems(player);
				player.setGameMode(GameMode.ADVENTURE);
				player.setAllowFlight(true);
				player.setFlying(true);
				
				playerMap.remove(player);
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
						Utils.clearInventory(player);
						player.teleport(getMap().getSpawn("is-" + i));
						playerMap.put(player, i);
						islandMap.put(player, i);
					}
				}
			}
		}
		
		applyRefill("feast", "feast");
		applyRefill("player-1", "player-1");
		applyRefill("player-2", "player-2");
		applyRefill("player-3", "player-3");
	
		setStage(GameStage.PREPARING);
		getSchedule().setTime(10);

		setStarted(System.currentTimeMillis());
		BukkitMain.getInstance().setTagControl(false);
	}
	
	@Override
	public void end()
	{
		int wid = 0;
		
		if (getStage() != GameStage.ENDING)
		{
			setStage(GameStage.ENDING);
			
			for (Player player : getPlayers())
			{
				int pid = playerMap.getOrDefault(player, -1);
				
				if (pid > 0)
				{
					player.setAllowFlight(true);
					player.setFlying(true);
					
					Language language = BattlePlayer.getLanguage(player.getUniqueId());					
					TitleAPI.setTitle(player, T.t(language, "skywars-victory-title"), T.t(language, "skywars-victory-subtitle"), 10, 100, 10, true);
					
					PlayerData winnerData = Main.getInstance().getPlayerManager().get(player);
					
					if (winnerData != null)
					{
						winnerData.addWin();
						winnerData.addTimePlayed();
						winnerData.executeUpdate();
					}
					
					if (wid > 0)
						continue;
					
					wid = pid;
				}
			}
			
			for (Player player : Bukkit.getOnlinePlayers())
			{
				Language language = BattlePlayer.getLanguage(player.getUniqueId());
				
				if (!playerMap.containsKey(player))
				{
					int tid = islandMap.getOrDefault(player, -1);
					
					if (tid > 0 && tid == wid) 
					{						
						TitleAPI.setTitle(player, T.t(language, "skywars-victory-title"), T.t(language, "skywars-victory-subtitle"), 10, 100, 10, true);

						PlayerData winnerData = Main.getInstance().getPlayerManager().get(player);
						
						if (winnerData != null)
						{
							winnerData.addWin();
							winnerData.executeUpdate();
						}
					}
					else
					{
						TitleAPI.setTitle(player, T.t(language, "skywars-lose-title"), T.t(language, "skywars-lose-subtitle"), 10, 100, 10, true);
					}
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
			Utils.shutdownDelayed(60);			
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
		if (playerMap.containsKey(player))
			return playerMap.get(player);
		if (islandMap.containsKey(player))
			return islandMap.get(player);
		return -1;	
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
	
		for (Party p : new HashSet<>(BattlebitsAPI.getPartyCommon().getPartys()))
		{
			BukkitParty party = (BukkitParty) p;
			Player owner = party.getBukkitOwner();
	
			if (owner != null && contains(owner) && !organized.contains(owner))
			{
				Iterator<Player> itr1 = party.getBukkitMembers().iterator();
				Iterator<Player> itr2 = playerMap.keySet().iterator();
				
				organized.add(owner);
				
				int toAdd = (max - 1);
				
				while (toAdd > 0)
				{
					if (itr1.hasNext())
					{
						Player member = itr1.next();
						
						if (contains(member) && !organized.contains(member))
						{
							organized.add(member);
							toAdd--;
						}
					}
					else if (itr2.hasNext())
					{
						Player queue = itr2.next();
						
						if (!organized.contains(queue))
						{
							organized.add(queue);
							toAdd--;
						}
					}
					else
					{
						break;
					}
				}
			}
		}
		
		playerMap.keySet().stream().filter(v -> !organized.contains(v)).forEach(v -> organized.add(v));

		return organized;
	}
}
