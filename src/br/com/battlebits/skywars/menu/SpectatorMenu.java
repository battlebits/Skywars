package br.com.battlebits.skywars.menu;

import br.com.battlebits.commons.api.item.ItemBuilder;
import br.com.battlebits.commons.api.menu.*;
import br.com.battlebits.commons.core.account.BattlePlayer;
import br.com.battlebits.commons.core.translate.T;
import br.com.battlebits.skywars.Main;
import br.com.battlebits.skywars.game.Engine;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * Arquivo criado em 31/03/2017.
 * Desenvolvido por:
 *
 * @author Luãn Pereira.
 */
public class SpectatorMenu extends MenuInventory {
    public SpectatorMenu(int pid, int rows) {
        super("Teleportador", rows);

        setUpdateHandler(new MenuUpdateHandler() {
            @Override
            public void onUpdate(Player player, MenuInventory inventory) {
                update(pid, rows);
            }
        });

        update(pid, rows);
    }

    private void update(int pid, int rows) {
        clear();
        Engine engine = Main.getInstance().getEngine();
        for (Player player : engine.getPlayers()) {
            int food = (player.getFoodLevel() * 100) / 20;
            int health = (int) ((player.getHealth() * 100) / player.getMaxHealth());
            addItem(new MenuItem(new ItemBuilder().type(Material.SKULL_ITEM).durability(3).name((pid != engine.getIsland(player) ? "§a§lAMIGO§a" : "§c§lINIMIGO§c") + " " + player.getName()).lore("§7Vida: §f" + health + "%", "§7Fome: §f" + food + "%", " ", "§7Clique para visualizar!").skin(player.getName()).build(), new MenuClickHandler() {
                @Override
                public void onClick(Player player, Inventory inv, ClickType type, ItemStack item, int slot) {
                    if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
                        Player target = Bukkit.getPlayer(ChatColor.stripColor(item.getItemMeta().getDisplayName()).split(" ")[1]);
                        if (target != null) {
                            player.sendMessage("§%command-teleport-prefix%§ " + T.t(BattlePlayer.getLanguage(player.getUniqueId()), "command-teleport-teleported-to-player", new String[]{"%player%", target.getName()}));
                            player.teleport(target);
                            player.closeInventory();
                            destroy(player);
                        }
                    }
                }
            }));
        }
    }
}
