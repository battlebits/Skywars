package br.com.battlebits.skywars.data;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;

public class PlayerManager 
{
	private Map<UUID, PlayerData> playerMap = new HashMap<>();

	public PlayerData get(Player player)
    {
        return playerMap.get(player.getUniqueId());
    }

	public PlayerData remove(Player player) 
	{
		return playerMap.remove(player.getUniqueId());
	}
	
	public void add(PlayerData data) 
	{
		playerMap.put(data.getUuid(), data);
	}
}
