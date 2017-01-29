package br.com.battlebits.skywars.utils;

import org.bukkit.entity.Player;

import lombok.Getter;
import lombok.Setter;

public class Combat {

	@Setter
	@Getter
	private Player damager;
	
	private long expire;
	
	public boolean isExpired()
	{
		return expire > System.currentTimeMillis();
	}
	
	public void setExpire(long time)
	{
		this.expire = System.currentTimeMillis() + time;
	}
}
