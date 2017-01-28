package br.com.battebits.skywars.game;

import java.util.Set;

import org.bukkit.entity.Player;

import com.google.gson.JsonObject;

import lombok.Getter;
import lombok.Setter;

public abstract class Engine {
	@Getter
	private GameType type;

	@Getter
	@Setter
	private GameStage stage;

	@Getter
	@Setter
	private GameSchedule schedule;

	@Getter
	@Setter
	private EngineMap map;

	@Getter
	@Setter
	private JsonObject items;
	
	@Getter
	@Setter
	private boolean insane;
	
	@Getter
	@Setter
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
