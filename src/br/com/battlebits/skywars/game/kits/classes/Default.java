package br.com.battlebits.skywars.game.kits.classes;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import br.com.battlebits.skywars.game.kits.Kit;

public class Default extends Kit
{
	public Default()
	{
		setName("Default");
		setIcon(new ItemStack(Material.WOOD_PICKAXE));
		addItem(new ItemStack(Material.WOOD_PICKAXE), false);
		addItem(new ItemStack(Material.WOOD_SPADE), false);
		addItem(new ItemStack(Material.WOOD_AXE), false);
	}
}
