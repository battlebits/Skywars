package br.com.battlebits.skywars.commands;

import br.com.battlebits.commons.bukkit.command.BukkitCommandArgs;
import br.com.battlebits.commons.core.command.CommandClass;
import br.com.battlebits.commons.core.command.CommandFramework.Command;
import br.com.battlebits.skywars.Main;

public class StartCommand implements CommandClass {
	@Command(name = "start")
	public void start(BukkitCommandArgs cmdArgs) {
		cmdArgs.getSender().sendMessage("Debug");
		Main.getInstance().getEngine().start();
	}
}
