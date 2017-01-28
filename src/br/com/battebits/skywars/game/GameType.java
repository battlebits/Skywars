package br.com.battebits.skywars.game;

import br.com.battebits.skywars.game.modes.Mega;
import br.com.battebits.skywars.game.modes.Solo;
import br.com.battebits.skywars.game.modes.Team;

public enum GameType {
	
	SOLO(Solo.class), //
	TEAM(Team.class), //
	MEGA(Mega.class); //

	private Class<? extends Engine> clazz;

	GameType(Class<? extends Engine> clazz) {
		this.clazz = clazz;
	}

	public String getName() {
		return clazz.getSimpleName();
	}
	
	public Engine newInstance() throws Exception {
		return clazz.newInstance();
	}
	
	public static Engine newInstance(String type) throws Exception {
		return valueOf(type).newInstance();
	}
}
