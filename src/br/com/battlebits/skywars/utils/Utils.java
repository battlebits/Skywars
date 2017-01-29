package br.com.battlebits.skywars.utils;

import java.util.Random;

import org.bukkit.entity.Player;

import br.com.battlebits.commons.BattlebitsAPI;
import br.com.battlebits.commons.core.account.BattlePlayer;
import br.com.battlebits.commons.core.translate.Language;

public class Utils {
	public static final Random RANDOM = new Random();
	
	public static Language getLanguage(Player player) {
		BattlePlayer bp = BattlebitsAPI.getAccountCommon().getBattlePlayer(player);
		return bp != null ? bp.getLanguage() : BattlebitsAPI.getDefaultLanguage();
	}
}
