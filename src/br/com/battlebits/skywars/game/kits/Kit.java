package br.com.battlebits.skywars.game.kits;

import br.com.battlebits.skywars.Main;
import br.com.battlebits.skywars.data.PlayerData;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.v1_8_R3.NBTTagCompound;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
public class Kit implements Listener 
{	
	private String name;	
	private ItemStack icon;
    private int price;

	private final Set<Player> players = new HashSet<>();
	private final Set<ItemStack> items = new HashSet<>();

	@Getter
	private static final List<Kit> kits = new ArrayList<>();
	
	public Kit()  
	{
		kits.add(this);
	}
	
	public void add(Player player)
	{
		players.add(player);
	}

	public void remove(Player player)
	{
		players.remove(player);
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
		
		NBTTagCompound tag = null;
		
		if (!nmsStack.hasTag())
		{
			tag = new NBTTagCompound();
			
			nmsStack.setTag(tag);
		}
		
		if (tag == null)
		{
			tag = nmsStack.getTag();
		}
		
		tag.setString("Kit", this.name);
		tag.setBoolean("Undroppable", undroppable);
				
		items.add(CraftItemStack.asBukkitCopy(nmsStack));
	}

	public boolean hasKit(Player player)
    {
        PlayerData pd = Main.getInstance().getPlayerManager().get(player);
        return (pd != null && pd.hasItem("Kit", this.name));
    }

	public boolean canBuy(Player player)
    {
        return false;
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
        	NBTTagCompound tag = CraftItemStack.asNMSCopy(item).getTag();
        	
        	if (tag != null && tag.hasKey("Kit"))
        	{
        		String name = tag.getString("Kit");
        		
        		if (kit.getName().equals(name))
        		{
        			found = true;
     
        			if (material != null)
        			{
        				found = found && item.getType().equals(material);
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
    
    public static boolean isUndroppable(ItemStack item)
    {
    	NBTTagCompound tag = CraftItemStack.asNMSCopy(item).getTag();
    	
    	return tag != null 
    			&& tag.hasKey("Kit")
    			&& tag.hasKey("Undroppable") 
    			&& tag.getBoolean("Undroppable");
    }

    public static Kit getKit(Player player)
    {
        for (Kit kit : kits)
        {
            if (kit.contains(player))
            {
                return kit;
            }
        }

        return getByName("Padrao");
    }

    public static Kit getByName(String name)
    {
        for (Kit kit : kits)
        {
            if (kit.getName().equalsIgnoreCase(name))
            {
                return kit;
            }
        }

        return null;
    }

    @Override
    public boolean equals(Object obj)
    {
        return (obj instanceof Kit) && ((Kit) obj).getName().equals(this.name);
    }
}
