package br.com.battebits.skywars.game;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.weather.WeatherChangeEvent;

import br.com.battlebits.commons.BattlebitsAPI;
import br.com.battlebits.commons.api.actionbar.ActionBarAPI;
import br.com.battlebits.commons.core.account.BattlePlayer;
import br.com.battlebits.commons.core.translate.Language;
import br.com.battlebits.commons.core.translate.T;

public class GameListener implements Listener {
	
	private Engine engine;
	
	public GameListener(Engine engine)
	{
		this.engine = engine;
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) 
	{
		Player player = event.getPlayer();
	
		
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		Player player = event.getPlayer();
		
		
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event)
	{
		
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEntityEvent event)
	{
		
	}
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event)
	{
		Player player = event.getEntity();
		
		if (engine.getStage() == GameStage.INGAME)
		{
			if (engine.contains(player))
			{
				int size = engine.getPlayers().size();
				
				engine.removePlayer(player);
				engine.checkCount();
				
				
				
				size--;
				
				if (size > 1)
				{
					for (Player other : Bukkit.getOnlinePlayers())
					{
						BattlePlayer bp = BattlebitsAPI.getAccountCommon().getBattlePlayer(other);
						
						if (bp != null)
						{
							Language lang = bp.getLanguage();
							
							ActionBarAPI.send(other, T.t(lang, "actionbar_players_remaining", new String[] {"%size%", ""+size+""}));
						}						
					}					
				}
			}
		}
		
		event.setDeathMessage(null);
		event.setDroppedExp(0);
	}
	
	@EventHandler
	public void onWeatherChange(WeatherChangeEvent event) 
	{
		if (event.toWeatherState()) 
		{
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onCreatureSpawn(CreatureSpawnEvent event) 
	{
		if (event.getSpawnReason() != SpawnReason.CUSTOM)
		{
			event.setCancelled(true);
		}
	}
}
