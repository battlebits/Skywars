package br.com.battebits.skywars.game;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.weather.WeatherChangeEvent;

import br.com.battebits.skywars.utils.BattleBoard;

public class GameListener implements Listener {
	
	private Engine engine;
	
	public GameListener(Engine engine) {
		this.engine = engine;
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		
		BattleBoard battleBoard = new BattleBoard(player);
		
		Map<Integer, String> rows = new HashMap<>();
		rows.put(15 - rows.size(), "");
		rows.put(15 - rows.size(), "");
		rows.put(15 - rows.size(), "");

		battleBoard.setRows(rows);
	}
	
	@EventHandler
	public void onWeatherChange(WeatherChangeEvent event) {
		if (event.toWeatherState()) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onCreatureSpawn(CreatureSpawnEvent event) {
		if (event.getSpawnReason() != SpawnReason.CUSTOM) {
			event.setCancelled(true);
		}
	}
}
