package br.com.battlebits.skywars.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import br.com.battlebits.skywars.Main;
import br.com.battlebits.skywars.game.Engine;
import br.com.battlebits.skywars.game.GameStage;

public class NameTag 
{
	private Player player;

	public NameTag(Player player)
	{
		this.player = player;
	}

	public void update()
	{
		Engine engine = Main.getInstance().getEngine();
		
		if (engine.getStage() != GameStage.PREGAME)
		{
			int t1 = engine.getIsland(player);
			
			for (Player observer : Bukkit.getOnlinePlayers())
			{
				Scoreboard scoreboard = observer.getScoreboard();

				for (Team team : scoreboard.getTeams())
                {
                    if (!team.getName().equals("a")
                    		&& !team.getName().equals("b")
                    		&& !team.getName().equals("c") 
                    		&& !team.getName().startsWith("row"))
                    {
                        team.unregister();
                    }
                }
				
				if (engine.contains(player) && t1 > 0)
				{
					int t2 = engine.getIsland(observer);

                    Team team = null;
					
					switch (engine.getType()) 
					{
					    case TEAM:
					    case MEGA:
					    {
					    	if (t1 != t2)
					    	{
					    		team = getTeam(scoreboard, "c", "§c[" + Utils.CHARS[t1 - 1] + "] ");
					    		
					    		addTeam(team, player.getName());
					    	}
					    	else
					    	{
					    		team = getTeam(scoreboard, "b", "§a[" + Utils.CHARS[t1 - 1] + "] ");
					    		
					    		addTeam(team, player.getName());
					    	}
					    	
					    	
					    	break;
					    }
						
					    case SOLO:
					    {
					    	if (t1 != t2)
					    	{
					    		team = getTeam(scoreboard, "c", "§c");
					    		
					    		addTeam(team, player.getName());
					    	}
					    	else
					    	{
					    		team = getTeam(scoreboard, "b", "§a");
					    		
					    		addTeam(team, player.getName());
					    	}
					    	
					    	break;
					    }
					}
				}
				else
				{
					addTeam(getTeam(scoreboard, "a", "§7"), player.getName());
					removeEntry(scoreboard.getTeam("b"), player.getName());
					removeEntry(scoreboard.getTeam("c"), player.getName());					
				}
			}
		}
	}
	
	private void addTeam(Team team, String entry)
	{
		if (team != null && !team.hasEntry(entry))
		{
			team.addEntry(entry);
		}
	}
	
	private void removeEntry(Team team, String entry)
	{
		if (team != null && team.hasEntry(entry))
		{
			team.removeEntry(entry);
		}
	}
	
	private Team getTeam(Scoreboard scoreboard, String name, String color)
	{
		Team team = scoreboard.getTeam(name);
		
		if (team == null)
		{
			team = scoreboard.registerNewTeam(name);
		}
		
		team.setPrefix(color);
		
		return team;
	}
}
