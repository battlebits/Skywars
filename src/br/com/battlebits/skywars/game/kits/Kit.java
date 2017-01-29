package br.com.battlebits.skywars.game.kits;

import java.util.HashSet;

import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.v1_8_R3.NBTTagCompound;

@Getter
@Setter
public class Kit implements Listener 
{	
	private String name;	
	private ItemStack icon;

	private final HashSet<Player> players = new HashSet<>();
	private final HashSet<ItemStack> items = new HashSet<>();
	private static final HashSet<Kit> KITS = new HashSet<>();
	
	public Kit()  
	{
		KITS.add(this);
	}
	
	public boolean contains(Player player)
	{
		return players.contains(player);
	}
	
	public void applyItems(Player player)
    {
        PlayerInventory inventory = player.getInventory();

        for (ItemStack item : items)
        {
            String type = item.getType().name();

            if (type.contains("_"))
            {
                String[] split = type.split("_");

                switch (split[1])
                {
                    case "HELMET":
                    {
                        inventory.setHelmet(item.clone());
                        break;
                    }

                    case "CHESTPLATE":
                    {
                        inventory.setChestplate(item.clone());
                        break;
                    }

                    case "LEGGINGS":
                    {
                        inventory.setLeggings(item.clone());
                        break;
                    }

                    case "BOOTS":
                    {
                        inventory.setBoots(item.clone());
                        break;
                    }

                    default:
                    {
                        inventory.addItem(item.clone());
                        break;
                    }
                }
            }
            else
            {
                inventory.addItem(item.clone());
            }
        }

        player.updateInventory();
    }
	
	public void addItem(ItemStack item, boolean undroppable)
	{
		net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
		
		NBTTagCompound compound = null;
		
		if (!nmsStack.hasTag())
		{
			compound = new NBTTagCompound();
			
			nmsStack.setTag(compound);
		}
		
		if (compound == null)
		{
			compound = nmsStack.getTag();
		}
		
		compound.setString("Kit", this.name);
		
		if (undroppable) compound.setBoolean("Undroppable", true);
		
		items.add(CraftItemStack.asBukkitCopy(nmsStack));
	}
	
	public static boolean isItem(Kit kit, ItemStack item)
    {
        return isItem(kit, item, null, (short) 0);
    }

    public static boolean isItem(Kit kit, ItemStack item, Material material)
    {
        return isItem(kit, item, material, (short) 0);
    }

    public static boolean isItem(Kit kit, ItemStack item, Material material, short data)
    {
        boolean found = false;

        if (item != null)
        {
            net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);

            NBTTagCompound compound = nmsStack.getTag();

            if (compound != null && compound.hasKey("Kit"))
            {
                String name = compound.getString("Kit");

                if (kit.getName().equals(name))
                {
                    found = true;
                    
                    if (material != null)
                    {
                        found = found && item.getType() == material;
                    }

                    if (data != 0)
                    {
                        found = found && item.getDurability() == data;
                    }
                }
            }
        }

        return found;
    }

    public static Kit getKit(Player player)
    {
        for (Kit kit : KITS)
        {
            if (kit.contains(player))
            {
                return kit;
            }
        }

        return getByName("Default");
    }

    public static Kit getByName(String name)
    {
        for (Kit kit : KITS)
        {
            if (kit.getName().equalsIgnoreCase(name))
            {
                return kit;
            }
        }

        return null;
    }
}
