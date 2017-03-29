package br.com.battlebits.skywars.menu.spectator;

import java.util.Arrays;

import br.com.battlebits.commons.api.menu.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import br.com.battlebits.commons.api.item.ItemBuilder;
import br.com.battlebits.commons.core.account.BattlePlayer;
import br.com.battlebits.commons.core.translate.T;
import br.com.battlebits.skywars.Main;
import br.com.battlebits.skywars.game.Engine;

public class SpectatorMenu extends MenuInventory
{
	private int pid;
	
	public SpectatorMenu(int pid, int rows) 
	{
		super("Teleportador", rows);

        this.pid = pid;
        update();

        setUpdateHandler(new MenuUpdateHandler() {
            @Override
            public void onUpdate(Player player, MenuInventory inventory) {
                update();
            }
        });
	}

	public void update()
	{
		super.clear();
		
		Engine engine = Main.getInstance().getEngine();
		
		for (Player player : engine.getPlayers()) 
		{
			addItem(new MenuItem(createSkull(engine, player), new MenuClickHandler()
			{
				@Override
				public void onClick(Player player, Inventory inv, ClickType type, ItemStack item, int slot)
				{
					if (item.getType() == Material.SKULL_ITEM && item.hasItemMeta() && item.getItemMeta().hasDisplayName())
					{
						Player target = Bukkit.getPlayer(ChatColor.stripColor(item.getItemMeta().getDisplayName()).replaceFirst("INIMIGO ", "").replaceFirst("AMIGO ", ""));
						
						if (target != null)
						{
							player.sendMessage("§%command-teleport-prefix%§ " + T.t(BattlePlayer.getLanguage(player.getUniqueId()), "command-teleport-teleported-to-player", new String[] {"%player%", target.getName()}));
							player.teleport(target);
							player.closeInventory();
							destroy(player);
						}
					}
				}
			}));
		}
	}
	
	private ItemStack createSkull(Engine engine, Player player)
	{		
		int food = (player.getFoodLevel() * 100) / 20;
        int health = (int) ((player.getHealth() * 100) / player.getMaxHealth());
		ItemStack skull = new ItemBuilder().type(Material.SKULL_ITEM).durability(3).name((pid != engine.getIsland(player) ? "§c§lINIMIGO§c " : "§a§lAMIGO§a ") + player.getName()).lore(Arrays.asList("§7Vida: §f" + health + "%", "§7Fome: §f" + food + "%", " ", "§7Clique para visualizar!")).build();
		SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
		skullMeta.setOwner(player.getName());
		skull.setItemMeta(skullMeta);		
		return skull;
	}
}
