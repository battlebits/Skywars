package br.com.battebits.skywars;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import br.com.battebits.skywars.data.MongoBackend;
import br.com.battebits.skywars.data.PlayerData;
import br.com.battebits.skywars.data.PlayerManager;
import br.com.battebits.skywars.game.Engine;
import br.com.battebits.skywars.game.EngineMap;
import br.com.battebits.skywars.game.EngineMap.Callback;
import br.com.battebits.skywars.game.GameListener;
import br.com.battebits.skywars.game.GameSchedule;
import br.com.battebits.skywars.game.GameType;
import br.com.battebits.skywars.utils.Utils;
import lombok.Getter;

public class Main extends JavaPlugin {
	
	@Getter
	private Engine engine;
	
	@Getter
	private PlayerManager playerManager;
	
	@Getter
	private MongoBackend mongoBackend;
	
	@Getter
	private static Main instance;

	@Override
	public void onLoad() {
		try {
			instance = this;
			JsonObject config = (JsonObject) readJson("config.json");
			JsonObject items = (JsonObject) readJson("items.json");
			
			String type = config.get("type").getAsString();
			boolean insane = config.get("insane").getAsBoolean();

			List<File> files = null;

			if ((files = getMaps(type, insane)) != null && !files.isEmpty()) {
				File file = files.get(Utils.RANDOM.nextInt(files.size()));
				EngineMap map = new EngineMap(file, new File("world"));
				map.unzip(new Callback() {
					@Override
					public void done(Exception e) {
						if (e != null) {
							logError("Erro ao extrair o mapa:", e);
							getServer().shutdown();
						} else {
							try {
								engine = GameType.newInstance(type);
								engine.setInsane(insane);
								engine.setItems(items);
								engine.setMap(map);
							} catch (Exception e1) {
								logError("Erro ao criar instancia:", e1);
								getServer().shutdown();
							}
						}
					}
				});
			} else {
				logWarn("Nenhum mapa foi encontrado!");
				getServer().shutdown();
			}
		} catch (Exception e) {
			logError("Erro ao carregar:", e);
			getServer().shutdown();
		}
	}

	@Override
	public void onEnable() {
		try {
			engine.getMap().enable();
			playerManager = new PlayerManager();
			mongoBackend = new MongoBackend();
			
			getServer().getScheduler().runTaskTimer(this, new GameSchedule(engine), 20L, 20L);
			getServer().getPluginManager().registerEvents(new GameListener(engine), this);
		} catch (Exception e) {
			logError("Erro ao habilitar:", e);
		}
	}

	@Override
	public void onDisable() {
		HandlerList.unregisterAll(this);
	}

	/* Plugin Logger */
	public void logInfo(String info) {
		getServer().getConsoleSender().sendMessage("§6[SkyWars] §b[INFO] " + info);
	}

	public void logWarn(String warn) {
		getServer().getConsoleSender().sendMessage("§6[SkyWars] §e[WARN] " + warn);
	}

	public void logError(String error) {
		getServer().getConsoleSender().sendMessage("§6[SkyWars] §c[ERROR] " + error);
	}

	public void logError(Exception e) {
		logError(null, e);
	}

	public void logError(String header, Exception e) {
		StringWriter writer = new StringWriter();

		if (header != null) {
			writer.write(header);
			writer.write("\n");
		}

		e.printStackTrace(new PrintWriter(writer));

		logError(writer.toString());
	}

	private JsonElement readJson(String name) throws IOException {
		File file = new File(getDataFolder(), name);

		logInfo("Lendo o arquivo §e\"" + file.getName() + "\" §b...");

		if (!file.exists()) {
			File parent = file.getParentFile();

			if (parent != null)
				parent.mkdirs();

			file.createNewFile();

			try (InputStream in = getResource(name)) {
				Files.copy(in, file.toPath());
			}
		}

		try (FileReader reader = new FileReader(file)) {
			JsonParser parser = new JsonParser();

			return parser.parse(reader);
		}
	}

	private List<File> getMaps(String directory, boolean insane) {
		directory = directory.toLowerCase();
		directory += (insane ? "_insane" : "");
		
		File folder = new File("../BSW-Maps/" + directory);
		if (folder.exists() && folder.isDirectory()) {
			List<File> files = new ArrayList<>();
			for (File file : folder.listFiles()) {
				if (file.isFile() && file.getName().endsWith(".dat")) {
					files.add(file);
				}
			}
			return files;
		}
		
		return null;
	}
	
	public static void main(String[] args) {
		System.out.println(new GsonBuilder().setPrettyPrinting().create().toJson(new PlayerData(UUID.randomUUID(), "MrLuangamer")));
	}
}
