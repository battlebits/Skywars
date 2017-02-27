package br.com.battlebits.skywars.game;

import br.com.battlebits.commons.core.server.loadbalancer.server.MinigameState;

public enum GameStage 
{
	PREGAME, PREPARING, INGAME, ENDING;
	
	public MinigameState toMinigameState()
	{
		return MinigameState.valueOf(name());
	}
}
