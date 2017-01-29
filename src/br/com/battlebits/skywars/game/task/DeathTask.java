package br.com.battlebits.skywars.game.task;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import br.com.battlebits.skywars.Main;
import br.com.battlebits.skywars.game.Engine;

public class DeathTask extends BukkitRunnable 
{
	private PlayerDeathEvent event;
	
	public DeathTask(Engine engine, PlayerDeathEvent event)
	{
		this.event = event;
		event.getEntity().setHealth(20D);
		event.getEntity().setMaxHealth(20D);
		runTaskLater(Main.getInstance(), 2L);
	}

	@Override
	public void run()
	{
		Player player = event.getEntity();
		player.getActivePotionEffects().forEach(v -> player.removePotionEffect(v.getType()));
		player.getInventory().setArmorContents(new ItemStack[4]);
		player.getInventory().clear();
		player.updateInventory();
	}
}
