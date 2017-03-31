package br.com.battlebits.skywars.game.kits.classes;

import br.com.battlebits.commons.api.item.ItemBuilder;
import br.com.battlebits.skywars.game.kits.Kit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerFishEvent;
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
        addItem(new ItemBuilder().type(Material.FISHING_ROD).amount(1).durability(10).name("%skywars-kit-fisherman-itemName%").build(), true);
    }

    @EventHandler
    public void onPlayerFish(PlayerFishEvent event) {
        Player player = event.getPlayer();

        // TODO: puxar o jogador para sua localidade.
    }
}
