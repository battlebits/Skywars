package br.com.battebits.skywars.game;

import java.util.Set;

import org.bukkit.entity.Player;

import com.google.gson.JsonObject;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class Engine {
	
	@Setter(AccessLevel.PRIVATE)
	private GameType type;

	private GameStage stage;

	private GameSchedule schedule;

	private EngineMap map;
	
	private JsonObject items;
	
	private boolean insane;
	
	private long started;

	public Engine(GameType type) {
		this.type = type;
	}

	public abstract void start();

	public abstract void end();

	public abstract void checkCount();

	public abstract boolean contains(Player player);

	public abstract void removePlayer(Player player);

	public abstract void addPlayer(Player player);

	public abstract int getIsland(Player player);

	public abstract Set<Player> getPlayers();
}
