package br.com.battlebits.skywars.bukkit.game.kits.classes;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import br.com.battlebits.skywars.bukkit.game.kits.Kit;

public class Padrao extends Kit {
    public Padrao() {
        setName("Padrao");
        setIcon(new ItemStack(Material.WOOD_PICKAXE));
        addItem(new ItemStack(Material.WOOD_PICKAXE), false);
        addItem(new ItemStack(Material.WOOD_SPADE), false);
        addItem(new ItemStack(Material.WOOD_AXE), false);
    }

    @Override
    public boolean hasKit(Player player) {
        return true;
    }

    @Override
    public boolean canBuy(Player player) {
        return false;
    }
}
