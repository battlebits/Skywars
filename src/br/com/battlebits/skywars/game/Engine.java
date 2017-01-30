package br.com.battlebits.skywars.game;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import br.com.battlebits.commons.api.item.ItemBuilder;
import br.com.battlebits.skywars.Main;
import br.com.battlebits.skywars.game.task.CageTask;
import br.com.battlebits.skywars.utils.Utils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class Engine {
	
	@Setter(AccessLevel.PRIVATE)
	private GameType type;
	private GameStage stage = GameStage.PREGAME;
	private GameSchedule schedule;
	
	private EngineMap map;
	private JsonObject items;
	
	private boolean insane;
	private long started;
	
	private HashSet<CageTask> cages = new HashSet<>();
	private Map<Player, Integer> killsMap = new HashMap<>();
	
	public Engine(GameType type) {
		this.type = type;
	}

	public abstract void start();

	public abstract void end();

	public abstract void checkCount();

	public abstract boolean contains(Player player);

	public abstract void removePlayer(Player player);

	public abstract void addPlayer(Player player);

	public abstract int getIsland(Player player);

	public abstract Set<Player> getPlayers();

	public void addCage(CageTask cageTask)
	{
		cages.add(cageTask);
	}
	
	public void addKill(Player player)
	{
		killsMap.put(player, getKills(player) + 1);
	}
	
	public int getKills(Player player)
	{
		int kills = 0;
		if (killsMap.containsKey(player))
			kills = killsMap.get(player);
		return kills;	
	}
	
	public void applyRefill(String target, String preset)
	{
		for (Block block : getMap().getChests(target))
		{
			try
			{
				block.getChunk().load(true);
				
				if (block.getState() instanceof Chest)
				{
					Chest chest = (Chest) block.getState();
					
					Inventory inventory = chest.getInventory();
					inventory.clear();

					JsonObject presets = items.getAsJsonObject(preset);
					List<Map.Entry<String, JsonElement>> entries = Lists.newArrayList(presets.entrySet());
					Map.Entry<String, JsonElement> entry = entries.get(Utils.RANDOM.nextInt(entries.size()));
					Iterator<JsonElement> iterator = ((JsonArray) entry.getValue()).iterator();
					
					while (iterator.hasNext())
					{
						JsonObject item = (JsonObject) iterator.next();
						
						Material material = Material.valueOf(item.get("type").getAsString());
						int amount = item.has("amount") ? item.get("amount").getAsInt() : 1;
						short data = item.has("data") ? item.get("data").getAsShort() : 0;
						
						ItemBuilder builder = new ItemBuilder().type(material).amount(amount).durability(data);
						
						if (item.has("enchantments"))
						{
							JsonObject enchantments = item.getAsJsonObject("enchantments");
							
							for (Map.Entry<String, JsonElement> ench : enchantments.entrySet())
							{
								Enchantment enchantment = Enchantment.getByName(ench.getKey());
								builder.enchantment(enchantment, ench.getValue().getAsInt());
							}
						}
						
						int slot = Utils.RANDOM.nextInt(27);
						
						while (inventory.getItem(slot) != null)
						{
							slot = Utils.RANDOM.nextInt(27);
						}
						
						inventory.setItem(slot, builder.build());
					}

					chest.update();
				}
			}
			catch (Exception e) 
			{
				Main.getInstance().logError("Erro ao aplicar refil no báu:", e);
			}
		}
	}
}
