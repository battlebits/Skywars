package br.com.battlebits.skywars.game.task;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.scheduler.BukkitRunnable;

import br.com.battlebits.skywars.Main;
import br.com.battlebits.skywars.game.Engine;
import br.com.battlebits.skywars.utils.Utils;

public class DeathTask extends BukkitRunnable {
    private Engine engine;
    private PlayerDeathEvent event;

    public DeathTask(Engine engine, PlayerDeathEvent event) {
        this.event = event;
        this.engine = engine;
        event.getEntity().setHealth(20D);
        event.getEntity().setMaxHealth(20D);
        runTaskLater(Main.getInstance(), 2L);
    }

    @Override
    public void run() {
        Player player = event.getEntity();
        Utils.clearInventory(player);
        Utils.addSpectatorItems(player);
        player.teleport(engine.getMap().getSpawn("spectators"));
        player.getActivePotionEffects().forEach(v -> player.removePotionEffect(v.getType()));
        player.setGameMode(GameMode.ADVENTURE);
        player.setAllowFlight(true);
        player.setFlying(true);
        player.setFireTicks(0);
        player.setFoodLevel(20);
        player.setLevel(0);
        player.setExp(0F);
    }
}
