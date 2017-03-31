package br.com.battlebits.skywars.utils;

import java.lang.reflect.Field;
import java.util.Random;

import br.com.battlebits.skywars.menu.KitInventory;
import org.bson.Document;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import br.com.battlebits.commons.BattlebitsAPI;
import br.com.battlebits.commons.api.item.ActionItemStack;
import br.com.battlebits.commons.api.item.ActionItemStack.InteractHandler;
import br.com.battlebits.skywars.Main;
import br.com.battlebits.skywars.game.Engine;
import br.com.battlebits.skywars.menu.SpecInventory;
import br.com.battlebits.commons.api.item.ItemBuilder;

public class Utils 
{
	public static final Random RANDOM = new Random();
    public static final char[] CHARS = new char[] {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};
    
    public static Document toDocument(Object object)
    {
    	return Document.parse(BattlebitsAPI.getGson().toJson(object));
    }
    
    public static void clearInventory(Player player)
    {
    	PlayerInventory inventory = player.getInventory();
    	
    	inventory.clear();
    	inventory.setArmorContents(new ItemStack[4]);
    	
    	player.updateInventory();
    }

    public static void addPlayerItems(Player player)
    {    	
    	PlayerInventory inventory = player.getInventory();
    	
    	inventory.setItem(0, new ActionItemStack(new ItemBuilder().type(Material.CHEST).name("§%skywars-kit-selector-item%§").build(), new InteractHandler() 
    	{
			@Override
			public boolean onInteract(Player player, ItemStack item, Action action) 
			{
                KitInventory kitInventory = new KitInventory(player);
                kitInventory.open(player);
				return false;
			}
		}).getItemStack());
    	
    	inventory.setItem(8, new ActionItemStack(new ItemBuilder().type(Material.TRIPWIRE_HOOK).name("§%skywars-player-leave-item%§").build(), new InteractHandler() 
    	{
			public boolean onInteract(Player player, ItemStack item, Action action) 
			{
				ByteArrayDataOutput out = ByteStreams.newDataOutput();
				out.writeUTF("Lobby");
				player.sendPluginMessage(Main.getInstance(), "BungeeCord", out.toByteArray());
				return false;
			}
		}).getItemStack());
    	
    	player.updateInventory();
    }
    
    public static void addSpectatorItems(Player player)
    {    	
    	PlayerInventory inventory = player.getInventory();
    	
    	inventory.setItem(0, new ActionItemStack(new ItemBuilder().type(Material.COMPASS).name("§%skywars-spectator-compass%§").build(), new InteractHandler() 
    	{
			@Override
			public boolean onInteract(Player player, ItemStack item, Action action) 
			{
				Engine engine = Main.getInstance().getEngine();
				int pid = engine.getIsland(player);
				int size = engine.getPlayers().size();
                int rows = (size <= 9) ? 1 : Math.round(size / 9);
				SpecInventory menu = new SpecInventory(pid, rows);
				menu.open(player);
				return false;
			}
		}).getItemStack());
    	
    	inventory.setItem(8, new ActionItemStack(new ItemBuilder().type(Material.TRIPWIRE_HOOK).name("§%skywars-player-leave-item%§").build(), new InteractHandler() 
    	{
			public boolean onInteract(Player player, ItemStack item, Action action) 
			{
				ByteArrayDataOutput out = ByteStreams.newDataOutput();
				out.writeUTF("Lobby");
				player.sendPluginMessage(Main.getInstance(), "BungeeCord", out.toByteArray());
				return false;
			}
		}).getItemStack());
    	
    	player.updateInventory();
    }

    public static Color getRandomColor()
    {
    	try
    	{
    		Field[] fields = Color.class.getFields();
    		return (Color) fields[RANDOM.nextInt(fields.length)].get(Color.class);
    	}
    	catch (Exception e) 
    	{
    		return null;
		}
    }
}
