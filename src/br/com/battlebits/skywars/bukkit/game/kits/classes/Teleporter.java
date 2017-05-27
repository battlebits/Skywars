package br.com.battlebits.skywars.bukkit.game.kits.classes;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import br.com.battlebits.commons.api.item.ItemBuilder;
import br.com.battlebits.commons.core.account.BattlePlayer;
import br.com.battlebits.commons.core.translate.T;
import br.com.battlebits.skywars.bukkit.Main;
import br.com.battlebits.skywars.bukkit.game.Engine;
import br.com.battlebits.skywars.bukkit.game.kits.Kit;

public class Teleporter extends Kit {
    private Map<UUID, Long> cooldown;

    public Teleporter() {
        setName("Teleporter");
        setIcon(new ItemStack(Material.ENDER_PEARL));
        addItem(new ItemBuilder().type(Material.ENDER_PEARL).name("§eTeleporter Kit").build(), true);
        this.cooldown = new HashMap<>();
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (contains(player) && isItem(this, event.getItem(), Material.ENDER_PEARL)) {
            long onExpire = cooldown.getOrDefault(player.getUniqueId(), 0L);

            if (onExpire > System.currentTimeMillis()) {
                double time = Math.round((-(System.currentTimeMillis() - onExpire) / 1000.0D) * 100.0D) / 100.0D;

                player.sendMessage(T.t(BattlePlayer.getLanguage(player.getUniqueId()), "skywars-kit-cooldown", new String[]{"%timeLeft%", Double.toString(time)}));
            } else {
                Engine engine = Main.getInstance().getEngine();

                int time = (60 - engine.getSchedule().getTime());

                if (time > 0) {
                    player.sendMessage(T.t(BattlePlayer.getLanguage(player.getUniqueId()), "skywars-kit-teleporter-wait", new String[]{"%timeLeft%", Integer.toString(time)}));
                } else if (time <= 0) {
                    player.launchProjectile(EnderPearl.class);
                    cooldown.put(player.getUniqueId(), System.currentTimeMillis() + 60000L);
                }
            }

            player.updateInventory();
            event.setCancelled(true);
        }
    }
}
