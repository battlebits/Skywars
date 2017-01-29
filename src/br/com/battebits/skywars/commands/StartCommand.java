package br.com.battebits.skywars.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import br.com.battebits.skywars.Main;

public class StartCommand implements CommandExecutor 
{
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) 
	{
		sender.sendMessage("Debug");
		Main.getInstance().getEngine().start();
		return false;
	}
}
