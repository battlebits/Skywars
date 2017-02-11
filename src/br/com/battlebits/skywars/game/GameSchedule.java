package br.com.battlebits.skywars.game;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import br.com.battlebits.commons.bukkit.scoreboard.BattleBoard;
import br.com.battlebits.commons.core.translate.Language;
import br.com.battlebits.skywars.Main;
import br.com.battlebits.skywars.data.PlayerData;
import br.com.battlebits.skywars.utils.NameTag;
import br.com.battlebits.skywars.utils.Utils;
import lombok.Getter;
import lombok.Setter;

public class GameSchedule implements Runnable {
	
	private Engine engine;
	
	@Getter
	@Setter
	private int time = 30, shutdown = 15;

	private static final int REFIL_1 = 10;
	private static final int REFIL_2 = 20;

	public GameSchedule(Engine engine) {
		engine.setSchedule(this);
		this.engine = engine;
	}

	@Override
	public void run() {
		switch (engine.getStage()) {
		case PREGAME: {
			int count = engine.getPlayers().size();
			int maxcount = Bukkit.getMaxPlayers();
			int percent = (count * 100) / maxcount;

			if (percent >= 100 && time > 10) {
				time = 10;
			}

			if (percent >= 80) {
				if (time > 0) {
					time--;

					if (time == 10) {
						// TODO
					} else if (time > 0 && time <= 5) {
						// TODO
					}
				} else {
					engine.start();
				}
			} else {
				time = 30;
			}

			break;
		}

		case PREPARING: {
			if (time > 0) {
				time--;
				engine.getCages().forEach(v -> v.run(time));
			} else {
				engine.setStage(GameStage.INGAME);
				engine.getCages().clear();
			}

			break;
		}

		case INGAME: {
			time++;

			switch (time) 
			{
			    case REFIL_1:
			    case REFIL_2:
			    {
			    	engine.applyRefill("feast", "feast-l2");
			    	engine.applyRefill("player-1", "player-1-l2");
			    	engine.applyRefill("player-2", "player-2-l2");
			    	engine.applyRefill("player-3", "player-3-l2");
			    	
			    	for (Player player : Bukkit.getOnlinePlayers())
			    	{
			    		player.playSound(player.getLocation(), Sound.CHEST_OPEN, 1F, 1.1F);
			    	}
			    	
			    	break;
			    }
			}
			
			break;
		}

		case ENDING: {
			if (shutdown > 0) {
				shutdown--;
			} else {
				Bukkit.shutdown();
			}

			break;
		}
		}
		
		updateScoreboard();
		updateVanished();
	}
	
	private void updateScoreboard()
	{
		Map<Integer, String> rows = new HashMap<>();
		
		for (Player player : Bukkit.getOnlinePlayers())
		{
			Language language = Utils.getLanguage(player);
			
			PlayerData data = Main.getInstance().getPlayerManager().get(player);
			
			if (data != null)
			{
				BattleBoard battleBoard = data.getBattleBoard();
				
				if (battleBoard != null)
				{
					switch (engine.getStage()) 
					{
					    case PREGAME:
					    {
							rows.put(15 - rows.size(), " ");
							rows.put(15 - rows.size(), "§7Kills: §b" + data.getKills());
							rows.put(15 - rows.size(), "§7Deaths: §b" + data.getDeaths());
							rows.put(15 - rows.size(), "§7XP: §b0");
							rows.put(15 - rows.size(), "§7Liga: §b-UNRANKED");
							rows.put(15 - rows.size(), " ");
							rows.put(15 - rows.size(), "§7Inicia em: §e" + time(time));
							rows.put(15 - rows.size(), "§7Servidor: §e" + Bukkit.getServerName());
							rows.put(15 - rows.size(), "§7Jogadores: §e" + engine.getPlayers().size() + "/" + Bukkit.getMaxPlayers());
							rows.put(15 - rows.size(), " ");
							rows.put(15 - rows.size(), "§7Kit: §bNenhum");
					    	break;
					    }
						
					    case PREPARING:
					    {
					    	break;
					    }
					    
					    default:
					    {
					    	break;
					    }
					}
					
					rows.put(15 - rows.size(), " ");
					rows.put(15 - rows.size(), "§ebattlebits.com.br");
					
					battleBoard.setDisplayName("§f§lBattle§6§lBits");
					battleBoard.setRows(rows);
					rows.clear();
				}
				
				NameTag nameTag = data.getNameTag();
				
				if (nameTag != null) nameTag.update();
			}
		}
	}
	
	private void updateVanished()
	{
		for (Player player : Bukkit.getOnlinePlayers())
		{
			for (Player other : Bukkit.getOnlinePlayers())
			{
				if (!other.equals(player))
				{
					if (!engine.contains(other))
					{
						if (player.canSee(other))
						{
							player.hidePlayer(other);
						}
					}
					else if (!player.canSee(other))
					{
						player.showPlayer(other);
					}
				}
			}
		}
	}
	
	private String time(int time)
	{
		return time / 60 + ":" + (time % 60 < 10 ? "0" : "") + time % 60;
	}
}
