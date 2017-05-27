package br.com.battlebits.skywars.bukkit.menu;

import br.com.battlebits.commons.api.item.ItemBuilder;
import br.com.battlebits.commons.api.menu.ClickType;
import br.com.battlebits.commons.api.menu.MenuClickHandler;
import br.com.battlebits.commons.api.menu.MenuInventory;
import br.com.battlebits.commons.api.menu.MenuItem;
import br.com.battlebits.commons.core.account.BattlePlayer;
import br.com.battlebits.skywars.bukkit.game.kits.Kit;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

/**
 * Arquivo criado em 30/03/2017.
 * Desenvolvido por:
 *
 * @author Luãn Pereira.
 */
public class KitInventory extends MenuInventory {
    public KitInventory(Player player) {
        this(player, Type.MY_KITS);
    }

    private KitInventory(Player player, Type type) {
        super(type.getTitle(), 6);

        setItem(0, new ItemBuilder().type(Material.INK_SACK).amount(1).durability(8).name("§cNão possui página anterior").build());
        setItem(8, new ItemBuilder().type(Material.INK_SACK).amount(1).durability(8).name("§cNão possui próxima página").build());

        setItem(3, new MenuItem(new ItemBuilder().type(Material.WOOL).durability(4).name("§eSeus kits").build(), new MenuClickHandler() {
            @Override
            public void onClick(Player player, Inventory inv, ClickType type, ItemStack item, int slot) {
                KitInventory inventory = new KitInventory(player, Type.MY_KITS);
                inventory.open(player);
            }
        }));

        setItem(5, new MenuItem(new ItemBuilder().type(Material.WOOL).durability(11).name("§9Todos os kits").build(), new MenuClickHandler() {
            @Override
            public void onClick(Player player, Inventory inv, ClickType type, ItemStack item, int slot) {
                KitInventory inventory = new KitInventory(player, Type.ALL_KITS);
                inventory.open(player);
            }
        }));

        setItem(4, new MenuItem(new ItemBuilder().type(Material.DIAMOND).name("§bLoja de kits").build(), new MenuClickHandler() {
            @Override
            public void onClick(Player player, Inventory inv, ClickType type, ItemStack item, int slot) {
                KitInventory inventory = new KitInventory(player, Type.STORE_KITS);
                inventory.open(player);
            }
        }));

        switch (type) {
            case MY_KITS: {
                for (int i = 9; i < 18; i++)
                    setItem(i, new ItemBuilder().type(Material.STAINED_GLASS_PANE).durability(4).name(" ").build());

                int slot = 18;

                for (Kit kit : Kit.getKits()) {
                    if (kit.hasKit(player)) {
                        setItem(slot++, new MenuItem(ItemBuilder.fromStack(kit.getIcon()).name("§e§l" + kit.getName()).lore(Arrays.asList("§aVocê possui este kit.", " ", "§7Sem descrição")).hideAttributes().build(), new MenuClickHandler() {
                            @Override
                            public void onClick(Player player, Inventory inv, ClickType type, ItemStack item, int slot) {
                                if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
                                    Kit kit = Kit.getByName(ChatColor.stripColor(item.getItemMeta().getDisplayName()));

                                    if (kit != null) {
                                        Kit currentKit = Kit.getKit(player);

                                        if (currentKit != null) {
                                            if (currentKit.equals(kit)) {
                                                player.sendMessage("§6§lKIT §fVocê já escolheu o kit §e§l" + kit.getName() + "§f.");
                                                player.closeInventory();
                                                return;
                                            }

                                            currentKit.remove(player);
                                        }

                                        player.sendMessage("§6§lKIT §fVocê escolheu o kit §e§l" + kit.getName() + "§f.");
                                        player.closeInventory();
                                        kit.add(player);
                                    }
                                }
                            }
                        }));
                    }
                }

                break;
            }

            case STORE_KITS: {
                for (int i = 9; i < 18; i++)
                    setItem(i, new ItemBuilder().type(Material.STAINED_GLASS_PANE).durability(3).name(" ").build());

                int slot = 18;

                for (Kit kit : Kit.getKits()) {
                    if (kit.getPrice() > 0 && kit.canBuy(player) && !kit.hasKit(player)) {
                        setItem(slot++, new MenuItem(ItemBuilder.fromStack(kit.getIcon()).name("§b§l" + kit.getName()).lore(Arrays.asList("§7Preço: §e" + kit.getPrice(), " ", "§7Sem descrição")).hideAttributes().build(), new MenuClickHandler() {
                            @Override
                            public void onClick(Player player, Inventory inv, ClickType type, ItemStack item, int slot) {
                                if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
                                    Kit kit = Kit.getByName(ChatColor.stripColor(item.getItemMeta().getDisplayName()));
                                    BattlePlayer bp = BattlePlayer.getPlayer(player.getUniqueId());

                                    if (kit != null && bp != null) {
                                        if (kit.getPrice() >= bp.getMoney()) {
                                            bp.removeMoney(kit.getPrice());
                                            player.sendMessage("§6§lKIT §fVocê comprou o kit §e§l" + kit.getName() + "§f.");
                                            player.closeInventory();
                                        } else {
                                            player.sendMessage("§6§lKIT §fVocê não dinheiro sufficiente.");
                                            player.closeInventory();
                                        }
                                    }
                                }
                            }
                        }));
                    }
                }

                break;
            }

            case ALL_KITS: {
                for (int i = 9; i < 18; i++)
                    setItem(i, new ItemBuilder().type(Material.STAINED_GLASS_PANE).durability(11).name(" ").build());

                int slot = 18;

                for (Kit kit : Kit.getKits()) {
                    boolean hasKit = kit.hasKit(player);

                    setItem(slot++, new MenuItem(ItemBuilder.fromStack(kit.getIcon()).name((hasKit ? "§e§l" : "§c§l") + kit.getName()).lore(Arrays.asList((hasKit ? "§aVocê possui este kit." : "§cVocê não tem este kit."), " ", "§7Sem descrição")).hideAttributes().build(), new MenuClickHandler() {
                        @Override
                        public void onClick(Player player, Inventory inv, ClickType type, ItemStack item, int slot) {
                            if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
                                Kit kit = Kit.getByName(ChatColor.stripColor(item.getItemMeta().getDisplayName()));

                                if (kit != null) {
                                    if (kit.hasKit(player)) {
                                        Kit currentKit = Kit.getKit(player);

                                        if (currentKit != null) {
                                            if (currentKit.equals(kit)) {
                                                player.sendMessage("§6§lKIT §fVocê já escolheu o kit §e§l" + kit.getName() + "§f.");
                                                player.closeInventory();
                                                return;
                                            }

                                            currentKit.remove(player);
                                        }

                                        player.sendMessage("§6§lKIT §fVocê escolheu o kit §e§l" + kit.getName() + "§f.");
                                        player.closeInventory();
                                        kit.add(player);
                                    } else {
                                        player.sendMessage("§6§lKIT §fVocê não possui o kit §e§l" + kit.getName() + "§f.");
                                        player.closeInventory();
                                    }
                                }
                            }
                        }
                    }));
                }

                break;
            }
        }
    }

    @RequiredArgsConstructor
    private enum Type {
        MY_KITS("§eSeus kits"),
        MONTH_KITS("Kits do mês"),
        STORE_KITS("§bLoja de kits"),
        FAVORITE_KITS("Kits favoritos"),
        ALL_KITS("§9Todos os kits");

        @Getter
        @NonNull
        private String title;
    }
}
