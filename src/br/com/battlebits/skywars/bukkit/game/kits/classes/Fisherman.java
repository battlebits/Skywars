package br.com.battlebits.skywars.bukkit.game.kits.classes;

import br.com.battlebits.commons.api.item.ItemBuilder;
import br.com.battlebits.skywars.bukkit.game.kits.Kit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerFishEvent.State;
import org.bukkit.inventory.ItemStack;

/**
 * Arquivo criado em 29/03/2017.
 * Desenvolvido por:
 *
 * @author Luãn Pereira.
 */
public class Fisherman extends Kit {
    public Fisherman() {
        setName("Fisherman");
        setIcon(new ItemStack(Material.FISHING_ROD));
        addItem(new ItemBuilder().type(Material.FISHING_ROD).amount(1).durability(15, true).name(ChatColor.YELLOW + "" + ChatColor.BOLD + "Fisherman").build(), true);
    }

    @EventHandler
    public void onPlayerFish(PlayerFishEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getItemInHand();
        if (contains(player) && isItem(this, item)) {
            if (event.getState() == State.FISHING) {
                // TODO: cooldown
            } else if (event.getState() == State.CAUGHT_ENTITY) {
                if (event.getCaught() instanceof Player) {
                    Player caught = (Player) event.getCaught();
                    caught.teleport(player.getLocation().clone());
                }
            }
        }
    }
}
