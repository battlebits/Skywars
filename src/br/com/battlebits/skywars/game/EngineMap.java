package br.com.battlebits.skywars.game;

import java.io.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import br.com.battlebits.commons.bukkit.tcbo3.BO3Common;
import br.com.battlebits.commons.bukkit.tcbo3.BO3Object;
import net.minecraft.server.v1_8_R3.DedicatedPlayerList;
import org.apache.commons.io.FileUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Monster;
import org.bukkit.entity.NPC;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import br.com.battlebits.skywars.Main;
import lombok.Getter;

public class EngineMap 
{
	@Getter
	private String name;
	@Getter
	private BO3Object lobby;
	private File file, folder;
	private Map<String, Location> spawns = new HashMap<>();
	private Map<String, List<Block>> chests = new HashMap<>();
	private Map<String, JsonElement> resources = new HashMap<>();

	public EngineMap(File file, File folder) throws Exception
	{
		this.file = file;
		this.folder = folder;
		this.name = file.getName()
				.replace("_", " ")
				.replaceFirst(".dat", "");
        extractFiles();
    }

	public void extractFiles() throws Exception
    {
        FileUtils.deleteDirectory(folder);
        if (!folder.exists()) folder.mkdirs();

        try (FileInputStream fis = new FileInputStream(file))
        {
            try (ZipInputStream zis = new ZipInputStream(fis))
            {
                ZipEntry ze = zis.getNextEntry();

                while (ze != null)
                {
                    String path = folder.getAbsolutePath() + File.separator + ze.getName();

                    if (!ze.isDirectory())
                    {
                        try (FileOutputStream fos = new FileOutputStream(path))
                        {
                            int len;
                            byte[] chunk = new byte[4096];
                            while ((len = zis.read(chunk)) > 0)
                                fos.write(chunk, 0, len);
                        }
                    }
                    else
                    {
                        File dir = new File(path);
                        if (!dir.exists()) dir.mkdir();
                    }

                    zis.closeEntry();
                    ze = zis.getNextEntry();
                }
            }
        }
    }

	public void startup(int perIsland) throws Exception
    {
        resources.put("chests", readJson("chests.json"));
        resources.put("spawns", readJson("spawns.json"));

        JsonObject spawns = (JsonObject) resources.get("spawns");
        int maxPlayers = spawns.entrySet().stream()
                .filter(e -> e.getKey().startsWith("is"))
                .mapToInt(e -> perIsland).sum();

        DedicatedPlayerList handle = ((CraftServer) Bukkit.getServer()).getHandle();
        Field field = handle.getClass().getSuperclass().getDeclaredField("maxPlayers");
        field.setAccessible(true);
        field.set(handle, maxPlayers);
    }

    public void postWorld() throws Exception
    {
        World world = Bukkit.getWorlds().get(0);
        world.setGameRuleValue("doDaylightCycle", "false");
        world.setGameRuleValue("showDeathMessages", "false");
        world.setStorm(false);
        world.setTime(6000L);

        for (Entity entity : world.getEntities())
            if ((entity instanceof Animals) || (entity instanceof Monster)
                    || (entity instanceof NPC) || (entity instanceof Item))
                entity.remove();

        JsonObject spawns = (JsonObject) resources.get("spawns");
        for (Map.Entry<String, JsonElement> entry : spawns.entrySet())
        {
            JsonObject spawn = (JsonObject) entry.getValue();
            double x = spawn.get("x").getAsDouble();
            double y = spawn.get("y").getAsDouble();
            double z = spawn.get("z").getAsDouble();
            float yaw = spawn.get("yaw").getAsFloat();
            float pitch = spawn.get("pitch").getAsFloat();
            this.spawns.put(entry.getKey(), new Location(world, x, y, z, yaw, pitch));
        }

        JsonObject chests = (JsonObject) resources.get("chests");
        for (Map.Entry<String, JsonElement> entry : chests.entrySet())
        {
            List<Block> blocks = this.chests.computeIfAbsent(entry.getKey(), v -> new ArrayList<>());

            JsonArray array = (JsonArray) entry.getValue();
            Iterator<JsonElement> iterator = array.iterator();

            while (iterator.hasNext())
            {
                JsonObject chest = (JsonObject) iterator.next();
                int x = chest.get("x").getAsInt();
                int y = chest.get("y").getAsInt();
                int z = chest.get("z").getAsInt();
                blocks.add(world.getBlockAt(x, y, z));
            }
        }

        lobby = BO3Common.parse(Main.getInstance().getResource("lobby.bo3"));
        lobby.paste(getSpawn("lobby").clone().subtract(0D, 2D, 0D));
    }

	private JsonElement readJson(String name) throws IOException 
	{
		File file = new File(folder, name);

		Main.getInstance().logInfo("Lendo o arquivo " + ChatColor.YELLOW + "\"" + file.getName() + "\" " + ChatColor.AQUA + "...");

		try (FileReader reader = new FileReader(file))
		{
			return new JsonParser().parse(reader);
		}
	}

	public boolean containsChests(String name)
	{
		return chests.containsKey(name);
	}

	public List<Block> getChests(String name)
	{
		return chests.get(name);
	}

    public Location getSpawn(String name)
    {
        return spawns.get(name);
    }
}
