package br.com.battebits.skywars.data;

import java.util.UUID;

import lombok.Getter;

@Getter
public class PlayerData {

	private UUID uuid;
	private String name;
	
	private int wins = 0;
	private int kills = 0;
	private int deaths = 0;
	private int assists = 0;
	
	public PlayerData(UUID uuid, String name) {
		this.uuid = uuid;
		this.name = name;
	}
}
