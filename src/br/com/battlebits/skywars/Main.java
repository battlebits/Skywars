package br.com.battlebits.skywars;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import br.com.battlebits.commons.util.ClassGetter;
import br.com.battlebits.skywars.game.kits.Kit;
import br.com.battlebits.skywars.game.listener.ChatListener;
import br.com.battlebits.skywars.game.listener.UpdateListener;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import br.com.battlebits.commons.bukkit.command.BukkitCommandFramework;
import br.com.battlebits.commons.core.command.CommandLoader;
import br.com.battlebits.skywars.data.mongodb.MongoBackend;
import br.com.battlebits.skywars.data.PlayerManager;
import br.com.battlebits.skywars.game.Engine;
import br.com.battlebits.skywars.game.EngineMap;
import br.com.battlebits.skywars.game.listener.GameListener;
import br.com.battlebits.skywars.game.GameSchedule;
import br.com.battlebits.skywars.game.GameType;
import br.com.battlebits.skywars.utils.Utils;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;

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

            JsonObject items = (JsonObject) readJson("items.json");
            JsonObject config = (JsonObject) readJson("config.json");
            JsonObject mongodb = config.getAsJsonObject("mongodb");

            this.mongoBackend = new MongoBackend(mongodb);
            this.mongoBackend.startConnection();

            String dir = config.get("dir").getAsString();
            String type = config.get("type").getAsString();
            boolean insane = config.get("insane").getAsBoolean();

            List<File> files = getMaps(dir);
            if (files != null && !files.isEmpty()) {
                if ((engine = GameType.getByName(type)) != null) {
                    File file = files.get(Utils.RANDOM.nextInt(files.size()));
                    EngineMap engineMap = new EngineMap(file, new File("world"));
                    engineMap.startup(engine.getType().getSizePerIsland());
                    engine.setMap(engineMap);
                    engine.setInsane(insane);
                    engine.setItems(items);
                } else {
                    logWarn("Modo \"" + type + "\" não encontrado!");
                    getServer().shutdown();
                }
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
        if (engine != null) {
            try {
                engine.getMap().postWorld();
                playerManager = new PlayerManager();

                if (Bukkit.getSpawnRadius() > 0)
                    Bukkit.setSpawnRadius(0);

                getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
                getServer().getPluginManager().registerEvents(new GameSchedule(engine), this);
                getServer().getPluginManager().registerEvents(new ChatListener(engine), this);
                getServer().getPluginManager().registerEvents(new GameListener(engine), this);
                getServer().getPluginManager().registerEvents(new UpdateListener(engine), this);

                new CommandLoader(new BukkitCommandFramework(this)).loadCommandsFromPackage(getFile(), "br.com.battlebits.skywars.commands");
                for (Class<?> clazz : ClassGetter.getClassesForPackageByFile(getFile(), "br.com.battlebits.skywars.game.kits.classes")) {
                    if (Kit.class.isAssignableFrom(clazz)) {
                        Kit kit = (Kit) clazz.newInstance();
                        logInfo("Registrando o kit §e\"" + kit.getName() + "\" §b...");
                        getServer().getPluginManager().registerEvents(kit, this);
                    }
                }

                logInfo("Carregado com sucesso!");
            } catch (Exception e) {
                logError("Erro ao habilitar:", e);
            }
        }
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
        if (mongoBackend != null) {
            mongoBackend.closeConnection();
        }
    }

    /* Plugin Logger */
    public void logInfo(String info) {
        getServer().getConsoleSender().sendMessage(ChatColor.GOLD + "[SkyWars] " + ChatColor.AQUA + "[INFO] " + info);
    }

    public void logWarn(String warn) {
        getServer().getConsoleSender().sendMessage(ChatColor.GOLD + "[SkyWars] " + ChatColor.YELLOW + "[WARN] " + warn);
    }

    public void logError(String error) {
        getServer().getConsoleSender().sendMessage(ChatColor.GOLD + "[SkyWars] " + ChatColor.RED + "[ERROR] " + error);
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

        logInfo("Lendo o arquivo " + ChatColor.YELLOW + "\"" + file.getName() + "\" " + ChatColor.AQUA + "...");

        if (!file.exists()) {
            File parent = file.getParentFile();

            if (parent != null)
                parent.mkdirs();

            try (InputStream in = getResource(name)) {
                Files.copy(in, file.toPath());
            }
        }

        try (FileReader reader = new FileReader(file)) {
            return new JsonParser().parse(reader);
        }
    }

    private List<File> getMaps(String directory) {
        File folder = new File(directory);
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
}
