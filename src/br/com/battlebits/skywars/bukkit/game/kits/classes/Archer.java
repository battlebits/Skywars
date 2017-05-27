package br.com.battlebits.skywars.bukkit.game.kits.classes;

import br.com.battlebits.commons.api.item.ItemBuilder;
import br.com.battlebits.skywars.bukkit.game.kits.Kit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Arquivo criado em 29/03/2017.
 * Desenvolvido por:
 *
 * @author Luãn Pereira.
 */
public class Archer extends Kit {
    public Archer() {
        setName("Archer");
        setIcon(new ItemStack(Material.BOW));
        addItem(new ItemBuilder().type(Material.BOW).build(), true);
        addItem(new ItemBuilder().type(Material.ARROW).amount(15).build(), true);
    }
}
