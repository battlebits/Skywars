package br.com.battebits.skywars.game;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import br.com.battebits.skywars.Main;
import br.com.battebits.skywars.data.PlayerData;
import br.com.battebits.skywars.utils.Utils;
import br.com.battlebits.commons.bukkit.scoreboard.BattleBoard;
import br.com.battlebits.commons.core.translate.Language;
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
			} else {
				// TODO
			}

			break;
		}

		case INGAME: {
			time++;

			// TODO

			break;
		}

		case ENDING: {
			if (shutdown > 0) {
				shutdown--;
			} else {

			}

			break;
		}
		}
		
		updateScoreboard();
	}
	
	public void updateScoreboard()
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
					
					battleBoard.setRows(rows);
					rows.clear();
				}
			}
		}
	}
}
