package br.com.battlebits.skywars.data;

import java.util.UUID;

import org.bson.Document;
import org.bukkit.entity.Player;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import br.com.battlebits.skywars.Main;
import br.com.battlebits.skywars.game.kits.Kit;
import br.com.battlebits.skywars.utils.Utils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import static br.com.battlebits.commons.BattlebitsAPI.getGson; 

@RequiredArgsConstructor
public class MatchData 
{
	@NonNull
	private UUID uuid;
	@Setter
	private long startTime;
	@Setter
	private long finishTime;
	@Setter
	private PlayerInfo winner;
	
	private JsonArray players = new JsonArray();
	private JsonArray deaths = new JsonArray();
	
	public void addDeath(UUID uuid, PlayerInfo killedBy)
	{
		JsonObject object = new JsonObject();
		object.addProperty("player", uuid.toString());
		object.add("killedBy", getGson().toJsonTree(killedBy));
		deaths.add(object);
	}
	
	public void addPlayer(Player player)
	{
		PlayerInfo info = getAsInfo(player);
		players.add(getGson().toJsonTree(info));		
	}
	
	public void executeUpdate()
	{
		Thread thread = new Thread(() -> {
			
			MongoBackend backend = Main.getInstance().getMongoBackend();
			MongoDatabase database = backend.getClient().getDatabase("skywars");
			MongoCollection<Document> collection = database.getCollection("matchs");
			collection.updateOne(Filters.eq("uuid", uuid.toString()), new Document("$set", Utils.toDocument(MatchData.this)));

		});
		
		thread.start();
	}
	
	public static MatchData create()
	{
		MongoBackend backend = Main.getInstance().getMongoBackend();
		MongoDatabase database = backend.getClient().getDatabase("skywars");
		MongoCollection<Document> collection = database.getCollection("matchs");

		/* Generate new UUID. */
		UUID uuid = null;
		do {
			uuid = UUID.randomUUID();
		} while (collection.find(Filters.eq("uuid", uuid.toString())).first() != null);
		collection.insertOne(new Document("uuid", uuid.toString()));
				
		return new MatchData(uuid);
	}
	
	public static PlayerInfo getAsInfo(Player player)
	{
		return new PlayerInfo(player.getUniqueId(), player.getName(), Kit.getKit(player).getName());
	}
	
	/**public static void main(String[] args)
	{
		MatchData data = MatchData.create();
		
		data.setStartTime(System.currentTimeMillis() - 60000L);
		data.setFinishTime(System.currentTimeMillis() + 60000L);
		
		data.addPlayer(new PlayerInfo(UUID.randomUUID(), "MrLuangamer", "Debug"));
		data.addPlayer(new PlayerInfo(UUID.randomUUID(), "Luluzinho_", "Debug"));
		data.addPlayer(new PlayerInfo(UUID.randomUUID(), "SrFalik", "Debug"));

		data.addDeath(UUID.randomUUID(), null);
		data.addDeath(UUID.randomUUID(), new PlayerInfo(UUID.randomUUID(), "SrFalik", "Debug"));
		data.addDeath(UUID.randomUUID(), new PlayerInfo(UUID.randomUUID(), "MrLuangamer", "Debug"));
		data.setWinner(new PlayerInfo(UUID.randomUUID(), "MrLuangamer", "Debug"));
		
		data.executeUpdate();
	}**/
	
	@RequiredArgsConstructor
	public static class PlayerInfo
	{
		@NonNull
		private UUID uuid;
		
		@NonNull
		private String name, kit;
	}
}
