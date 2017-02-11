package br.com.battlebits.skywars.commands;

import org.bukkit.entity.Player;

import br.com.battlebits.commons.bukkit.command.BukkitCommandArgs;
import br.com.battlebits.commons.core.command.CommandClass;
import br.com.battlebits.commons.core.command.CommandFramework.Command;
import br.com.battlebits.skywars.Main;
import br.com.battlebits.skywars.game.Engine;
import br.com.battlebits.skywars.game.GameStage;

public class StartCommand implements CommandClass 
{
	@Command(name = "start")
	public void start(BukkitCommandArgs cmdArgs) 
	{
		if (cmdArgs.isPlayer())
		{
			String[] args = cmdArgs.getArgs();
			
			Player player = cmdArgs.getPlayer();
			
			if (args.length == 0)
			{
				Engine engine = Main.getInstance().getEngine();
				
				if (engine.getStage() == GameStage.PREGAME)
				{
					engine.start();
				}
				else
				{
					// A partida está em andamento
				}
			}
			else
			{
				// Uso correto
			}
		}
		else
		{
			// Console
		}
	}
}
