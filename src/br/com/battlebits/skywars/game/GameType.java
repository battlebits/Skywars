package br.com.battlebits.skywars.game;

import br.com.battlebits.skywars.game.modes.Mega;
import br.com.battlebits.skywars.game.modes.Solo;
import br.com.battlebits.skywars.game.modes.Team;

public enum GameType 
{
	SOLO(Solo.class), //
	TEAM(Team.class), //
	MEGA(Mega.class); //

	private Class<? extends Engine> clazz;

	GameType(Class<? extends Engine> clazz)
	{
		this.clazz = clazz;
	}

	public String getName() 
	{
		return clazz.getSimpleName();
	}
	
	public Engine newInstance() throws Exception 
	{
		return clazz.newInstance();
	}
	
	public static Engine newInstance(String type) throws Exception 
	{
		return valueOf(type).newInstance();
	}
	
	public int getSizePerIsland()
	{
		switch (this) 
		{
		    case MEGA:
			    return 5;
		    case TEAM:
		    	return 2;
		    default:
		    	return 1;
		}
	}
}
