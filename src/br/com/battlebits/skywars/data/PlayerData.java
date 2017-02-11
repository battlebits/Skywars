package br.com.battlebits.skywars.data;

import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.bson.Document;
import org.bukkit.entity.Player;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import br.com.battlebits.commons.bukkit.scoreboard.BattleBoard;
import br.com.battlebits.skywars.Main;
import br.com.battlebits.skywars.utils.Combat;
import br.com.battlebits.skywars.utils.NameTag;
import lombok.Getter;

@Getter
public class PlayerData 
{
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
	
	public PlayerData(UUID uuid, String name)
	{
		this.uuid = uuid;
		this.name = name;
		this.combat = new Combat();
		this.items = new JsonArray();
	}
	
	public Combat getCombat()
	{
		return combat;
	}
	
	public void addWin()
	{
		wins++;
	}
	
	public void addKill()
	{
		kills++;
	}
	
	public void addDeath()
	{
		deaths++;
	}
	
	public void addAssist()
	{
		assists++;
	}
	
	public void addTimePlayed()
	{
		long started = Main.getInstance().getEngine().getStarted();
		long difference = System.currentTimeMillis() - started;
		timePlayed += TimeUnit.MILLISECONDS.toSeconds(difference);
	}

	public void update()
	{
		Thread thread = new Thread(new Runnable() 
		{
			@Override
			public void run() 
			{
				MongoBackend backend = Main.getInstance().getMongoBackend();
				MongoDatabase database = backend.getClient().getDatabase("skywars");
				MongoCollection<Document> collection = database.getCollection("data");
				
				Document document = new Document();
				document.append("wins", wins);
				document.append("kills", kills);
				document.append("deaths", deaths);
				document.append("assists", assists);
				document.append("timePlayed", timePlayed);
				
				collection.updateOne(Filters.eq("uuid", uuid.toString()), new Document("$set", document));
			}
		});
		
		thread.start();
	}

	public void onJoin(Player player)
	{
		nameTag = new NameTag(player);
		battleBoard = new BattleBoard(player);
		
		if (!getName().equals(player.getName()))
		{
			Thread thread = new Thread(new Runnable() 
			{
				@Override
				public void run() 
				{
					MongoBackend backend = Main.getInstance().getMongoBackend();
					MongoDatabase database = backend.getClient().getDatabase("skywars");
					MongoCollection<Document> collection = database.getCollection("data");
					collection.updateOne(Filters.eq("uuid", uuid.toString()), new Document("$set", new Document("name", player.getName())));
				}
			});
			
			thread.start();
		}
	}
	
	public boolean hasItem(String type, String item)
	{
		boolean found = false;
		
		Iterator<JsonElement> iterator = items.iterator();
		
		while (iterator.hasNext())
		{
			boolean check = false;
			
			JsonObject object = (JsonObject) iterator.next();
			
			if (object.has("expire"))
			{
				long expire = object.get("expire").getAsLong();
				
				if (expire < System.currentTimeMillis())
				{
					check = true;
				}
			}
			else if (object.has("lifetime"))
			{
				check = object.get("lifetime").getAsBoolean();
			}
			
			if (check && object.has("type") && object.has("item"))
			{
				String otherType = object.get("type").getAsString();
				String otherItem = object.get("item").getAsString();
				found = otherType.equals(type) && otherItem.equals(item);
			}
		}
		
		return found;
	}
	
	public void addItem(String type, String item)
	{
		addItem(type, item, true, 0L);
	}
	
	public void addItem(String type, String item, boolean lifetime, long expire)
	{
		JsonObject object = new JsonObject();
		
		object.addProperty("type", type);
		object.addProperty("item", item);
		
		if (lifetime)
			object.addProperty("lifetime", lifetime);
		else 
			object.addProperty("expire", expire);	
		
		items.add(object);
		
		Thread thread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				Main.getInstance().logWarn(items.toString());
				
				MongoBackend backend = Main.getInstance().getMongoBackend();
				MongoDatabase database = backend.getClient().getDatabase("skywars");
				MongoCollection<Document> collection = database.getCollection("data");
				
				collection.updateOne(Filters.eq("uuid", uuid.toString()), new Document("$set", new Document("items", Document.parse(items.toString()))));
			}
		});
		
		thread.start();
	}
}
