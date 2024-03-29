package br.com.battlebits.skywars.bukkit.data;

import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import br.com.battlebits.skywars.bukkit.data.mongodb.MongoBackend;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import br.com.battlebits.commons.bukkit.scoreboard.BattleBoard;
import br.com.battlebits.skywars.bukkit.Main;
import br.com.battlebits.skywars.bukkit.utils.Combat;
import br.com.battlebits.skywars.bukkit.utils.NameTag;
import br.com.battlebits.skywars.bukkit.utils.Utils;
import lombok.Getter;

@Getter
public class PlayerData {
    private UUID uuid;
    private String name;

    private int wins = 0;
    private int kills = 0;
    private int deaths = 0;
    private int assists = 0;
    private int timePlayed = 0;
    private JsonArray items;

    private transient Combat combat;
    private transient NameTag nameTag;
    private transient BattleBoard battleBoard;

    public PlayerData(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
        this.items = new JsonArray();
    }

    public Combat getCombat() {
        return combat;
    }

    public void addWin() {
        wins++;
    }

    public void addKill() {
        kills++;
    }

    public void addDeath() {
        deaths++;
    }

    public void addAssist() {
        assists++;
    }

    public void addTimePlayed() {
        long started = Main.getInstance().getEngine().getStarted();
        long difference = System.currentTimeMillis() - started;
        timePlayed += TimeUnit.MILLISECONDS.toSeconds(difference);
    }

    public void onJoin(Player player) {
        combat = new Combat();
        nameTag = new NameTag(player);
        battleBoard = new BattleBoard(player);

        if (!getName().equals(player.getName())) {
            Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () ->
            {
                MongoBackend backend = Main.getInstance().getMongoBackend();
                MongoDatabase database = backend.getClient().getDatabase("skywars");
                MongoCollection<Document> collection = database.getCollection("data");
                collection.updateOne(Filters.eq("uuid", uuid.toString()), new Document("$set", new Document("name", player.getName())));
            });
        }
    }

    public boolean hasItem(String type, String item) {
        boolean found = false;

        Iterator<JsonElement> iterator = items.iterator();

        while (iterator.hasNext()) {
            boolean valid = false;

            JsonObject object = (JsonObject) iterator.next();

            if (object.has("expire")) {
                long expire = object.get("expire").getAsLong();

                if (expire > System.currentTimeMillis()) {
                    valid = true;
                }
            } else if (object.has("lifetime")) {
                valid = object.get("lifetime").getAsBoolean();
            }

            if (valid && object.has("type") && object.has("item")) {
                String otherType = object.get("type").getAsString();
                String otherItem = object.get("item").getAsString();
                found |= otherType.equals(type) && otherItem.equals(item);
            }
        }

        return found;
    }

    public void addItem(String type, String item) {
        addItem(type, item, true, 0L);
    }

    public void addItem(String type, String item, long expire) {
        addItem(type, item, false, expire);
    }

    private void addItem(String type, String item, boolean lifetime, long expire) {
        if (hasItem(type, item)) return;

        JsonObject object = new JsonObject();

        object.addProperty("type", type);
        object.addProperty("item", item);

        if (lifetime)
            object.addProperty("lifetime", lifetime);
        else
            object.addProperty("expire", expire);

        items.add(object);

        executeUpdate();
    }

    public void executeUpdate() {
        Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () ->
        {
            MongoBackend backend = Main.getInstance().getMongoBackend();
            MongoDatabase database = backend.getClient().getDatabase("skywars");
            MongoCollection<Document> collection = database.getCollection("data");
            collection.updateOne(Filters.eq("uuid", uuid.toString()), new Document("$set", Utils.toDocument(PlayerData.this)));
        });
    }

    public static PlayerData getPlayer(Player player) {
        return Main.getInstance().getPlayerManager().get(player);
    }
}
