package br.com.battlebits.skywars.game.task;

import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.scheduler.BukkitRunnable;

import br.com.battlebits.skywars.Main;
import br.com.battlebits.skywars.game.Engine;

public class DeathTask extends BukkitRunnable 
{
	public DeathTask(Engine engine, PlayerDeathEvent event)
	{
		event.getEntity().setHealth(20D);
		event.getEntity().setMaxHealth(20D);
		runTaskLater(Main.getInstance(), 2L);
	}

	@Override
	public void run()
	{

	}
}
