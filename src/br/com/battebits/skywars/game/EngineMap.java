package br.com.battebits.skywars.game;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import br.com.battebits.skywars.Main;
import lombok.Getter;
import net.minecraft.server.v1_8_R3.DedicatedPlayerList;

public class EngineMap {
	
	@Getter
	private String name;
	private File file, folder;
	private Map<String, Location> spawns = new HashMap<>();
	private Map<String, List<Block>> chests = new HashMap<>();

	public EngineMap(File file, File folder) {
		this.file = file;
		this.folder = folder;
		this.name = file.getName().replace("_", " ").replaceFirst(".dat", "");
	}

	public void unzip(Callback callback) {
		try {
			FileUtils.deleteDirectory(folder);

			if (!folder.exists())
				folder.mkdir();

			try (FileInputStream fis = new FileInputStream(file)) {
				try (ZipInputStream zis = new ZipInputStream(fis)) {
					ZipEntry entry = zis.getNextEntry();

					while (entry != null) {
						String path = folder.getAbsolutePath() + File.separator + entry.getName();

						if (!entry.isDirectory()) {
							try (FileOutputStream fos = new FileOutputStream(path)) {
								int len = 0;

								byte[] buffer = new byte[1024];

								try (BufferedOutputStream bos = new BufferedOutputStream(fos)) {
									while ((len = zis.read(buffer)) != -1) {
										bos.write(buffer, 0, len);
									}
								}
							}
						} else {
							File dir = new File(path);

							if (!dir.exists())
								dir.mkdir();
						}

						zis.closeEntry();
						entry = zis.getNextEntry();
					}
				}
			}

			callback.done(null);
		} catch (Exception e) {
			callback.done(e);
		}
	}

	public interface Callback {
		void done(Exception e);
	}

	public void enable() {
		try {
			World world = Bukkit.getWorlds().get(0);
			Engine engine = Main.getInstance().getEngine();

			JsonObject object = (JsonObject) readJson("spawns.json");

			for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
				JsonObject spawn = (JsonObject) entry.getValue();

				double x = spawn.get("x").getAsDouble();
				double y = spawn.get("y").getAsDouble();
				double z = spawn.get("z").getAsDouble();

				float yaw = spawn.get("yaw").getAsFloat();
				float pitch = spawn.get("pitch").getAsFloat();
				
				spawns.put(entry.getKey(), new Location(world, x, y, z, yaw, pitch));
			}

			object = (JsonObject) readJson("chests.json");

			for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
				chests.put(entry.getKey(), new ArrayList<>());

				List<Block> blocks = chests.get(entry.getKey());

				JsonArray array = (JsonArray) entry.getValue();
				Iterator<JsonElement> iterator = array.iterator();

				while (iterator.hasNext()) {
					JsonObject chest = (JsonObject) iterator.next();

					int x = chest.get("x").getAsInt();
					int y = chest.get("y").getAsInt();
					int z = chest.get("z").getAsInt();

					blocks.add(world.getBlockAt(x, y, z));
				}
			}

			int max = (int) spawns.entrySet().stream().filter(entry -> entry.getKey().startsWith("is")).count();

			switch (engine.getType()) {
			case TEAM:
				max *= 2;
				break;
			case MEGA:
				max *= 5;
				break;
			default:
				break;
			}

			DedicatedPlayerList handle = ((CraftServer) Bukkit.getServer()).getHandle();
			Field field = handle.getClass().getSuperclass().getDeclaredField("maxPlayers");
			field.setAccessible(true);
			field.set(handle, max);
		} catch (Exception e) {
			Main.getInstance().logError("Erro ao habilitar o mapa:", e);
		}
	}

	private JsonElement readJson(String name) throws IOException {
		File file = new File(folder, name);

		Main.getInstance().logInfo("Lendo o arquivo §e\"" + file.getName() + "\" §b...");

		try (FileReader reader = new FileReader(file)) {
			JsonParser parser = new JsonParser();

			return parser.parse(reader);
		}
	}
	
	public Location getSpawn(String name)
	{
		return spawns.get(name);
	}
}
