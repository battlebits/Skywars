package br.com.battlebits.skywars.bukkit.game.modes;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import com.google.common.collect.Iterables;

import br.com.battlebits.commons.api.title.TitleAPI;
import br.com.battlebits.commons.bukkit.BukkitMain;
import br.com.battlebits.commons.core.account.BattlePlayer;
import br.com.battlebits.commons.core.translate.Language;
import br.com.battlebits.commons.core.translate.T;
import br.com.battlebits.skywars.bukkit.Main;
import br.com.battlebits.skywars.bukkit.data.PlayerData;
import br.com.battlebits.skywars.bukkit.game.Engine;
import br.com.battlebits.skywars.bukkit.game.GameStage;
import br.com.battlebits.skywars.bukkit.game.GameType;
import br.com.battlebits.skywars.bukkit.game.task.CageTask;
import br.com.battlebits.skywars.bukkit.utils.Utils;

public class Solo extends Engine {
    private Map<Player, Integer> playerMap = new HashMap<>();

    public Solo() {
        super(GameType.SOLO);
    }

    @Override
    public void start() {
        Set<Player> players = new HashSet<>(playerMap.keySet());
        Iterator<Player> iterator = players.iterator();

        for (int i = 1; iterator.hasNext(); i++) {
            Player player = iterator.next();

            if (i > Bukkit.getMaxPlayers()) {
                Utils.clearInventory(player);
                Utils.addSpectatorItems(player);
                player.setGameMode(GameMode.ADVENTURE);
                player.setAllowFlight(true);
                player.setFlying(true);
                playerMap.remove(player);
            } else {
                playerMap.put(player, i);
                addCage(new CageTask(this, getMap().getSpawn("is-" + i), player));
            }
        }

        applyRefill("feast", "feast");
        applyRefill("subfeast", "subfeast");
        applyRefill("player-1", "player-1");
        applyRefill("player-2", "player-2");
        applyRefill("player-3", "player-3");

        setStage(GameStage.PREPARING);
        getSchedule().setTime(10);
        getMap().getLobby().undo();

        setStarted(System.currentTimeMillis());
        BukkitMain.getInstance().setTagControl(false);
    }

    @Override
    public void end() {
        if (getStage() != GameStage.ENDING) {
            setStage(GameStage.ENDING);

            Player winner = Iterables.getFirst(playerMap.keySet(), null);
            PlayerData winnerData = Main.getInstance().getPlayerManager().get(winner);

            if (winnerData != null) {
                winnerData.addWin();
                winnerData.addTimePlayed();
                winnerData.executeUpdate();
            }

            winner.setAllowFlight(true);
            winner.setFlying(true);

            Language language = BattlePlayer.getLanguage(winner.getUniqueId());
            TitleAPI.setTitle(winner, T.t(language, "skywars-victory-title"), T.t(language, "skywars-victory-subtitle"), 10, 100, 10, true);

            for (Player other : Bukkit.getOnlinePlayers()) {
                if (!other.equals(winner)) {
                    language = BattlePlayer.getLanguage(winner.getUniqueId());
                    TitleAPI.setTitle(other, T.t(language, "skywars-lose-title"), T.t(language, "skywars-lose-subtitle"), 10, 100, 10, true);
                }
            }
        }
    }

    @Override
    public void checkCount() {
        switch (playerMap.size()) {
            case 0: {
                Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> Bukkit.shutdown(), 60L);
                break;
            }

            case 1: {
                end();
                break;
            }
        }
    }

    @Override
    public boolean contains(Player player) {
        return playerMap.containsKey(player);
    }

    @Override
    public int getIsland(Player player) {
        return playerMap.getOrDefault(player, -1);
    }

    @Override
    public void addPlayer(Player player) {
        playerMap.put(player, -1);
    }

    @Override
    public void removePlayer(Player player) {
        playerMap.remove(player);
    }

    @Override
    public Set<Player> getPlayers() {
        return playerMap.keySet();
    }

    @Override
    public int getPlayerCount() {
        return playerMap.size();
    }

    @Override
    public int getTeamCount() {
        return -1;
    }
}
