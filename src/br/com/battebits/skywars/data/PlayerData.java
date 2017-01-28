package br.com.battebits.skywars.data;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.bson.Document;
import org.bukkit.entity.Player;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import br.com.battebits.skywars.Main;
import br.com.battebits.skywars.game.Engine;
import br.com.battebits.skywars.utils.Combat;
import br.com.battlebits.commons.bukkit.scoreboard.BattleBoard;
import lombok.Getter;
import lombok.Setter;

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
	
	@Setter
	private transient BattleBoard battleBoard;
	private transient Combat combat = new Combat();
	
	public PlayerData(UUID uuid, String name)
	{
		this.uuid = uuid;
		this.name = name;
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
		Engine engine = Main.getInstance().getEngine();
		long difference = System.currentTimeMillis() - engine.getStarted();
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
	
	public Combat getCombat()
	{
		return combat;
	}
	
	public void onJoin(Player player)
	{
		battleBoard = new BattleBoard(player);
	}
}
