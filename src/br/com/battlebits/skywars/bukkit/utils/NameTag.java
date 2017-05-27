package br.com.battlebits.skywars.bukkit.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import br.com.battlebits.commons.core.account.BattlePlayer;
import br.com.battlebits.commons.core.translate.T;
import br.com.battlebits.skywars.bukkit.Main;
import br.com.battlebits.skywars.bukkit.game.Engine;
import br.com.battlebits.skywars.bukkit.game.GameStage;

public class NameTag {
    private Player player;

    private static final String SPECTATOR = "a";
    private static final String PLAYER = "b";
    private static final String ENEMY = "c";

    public NameTag(Player player) {
        this.player = player;
    }

    public void update() {
        Engine engine = Main.getInstance().getEngine();

        if (engine.getStage() != GameStage.PREGAME) {
            int pid = engine.getIsland(player);

            Scoreboard mainScoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

            for (Player observer : Bukkit.getOnlinePlayers()) {
                Scoreboard scoreboard = observer.getScoreboard();

                if (!scoreboard.equals(mainScoreboard)) {
                    for (Team team : scoreboard.getTeams()) {
                        if (!team.getName().equals(SPECTATOR)
                                && !team.getName().equals(PLAYER)
                                && !team.getName().equals(ENEMY)
                                && !team.getName().startsWith("row")) {
                            team.unregister();
                        }
                    }

                    if (engine.contains(player) && pid > 0) {
                        int oid = engine.getIsland(observer);

                        if (pid != oid)
                            addTeam(getTeam(scoreboard, ENEMY, "§c§l" + T.t(BattlePlayer.getLanguage(observer.getUniqueId()), "skywars-tag-enemy") + "§c "), player.getName());
                        else
                            addTeam(getTeam(scoreboard, PLAYER, "§a§l" + T.t(BattlePlayer.getLanguage(observer.getUniqueId()), "skywars-tag-friend") + "§a "), player.getName());
                    } else {
                        addTeam(getTeam(scoreboard, SPECTATOR, "§7§l" + T.t(BattlePlayer.getLanguage(observer.getUniqueId()), "skywars-tag-spectator") + "§7 "), player.getName());
                        removeEntry(scoreboard.getTeam(PLAYER), player.getName());
                        removeEntry(scoreboard.getTeam(ENEMY), player.getName());
                    }
                }
            }
        }
    }

    private void addTeam(Team team, String entry) {
        if (team != null && !team.hasEntry(entry)) {
            team.addEntry(entry);
        }
    }

    private void removeEntry(Team team, String entry) {
        if (team != null && team.hasEntry(entry)) {
            team.removeEntry(entry);
        }
    }

    private Team getTeam(Scoreboard scoreboard, String name, String color) {
        Team team = scoreboard.getTeam(name);

        if (team == null)
            team = scoreboard.registerNewTeam(name);

        if (color.length() > 16)
            color = color.substring(0, 16);

        team.setPrefix(color);
        team.setSuffix("");

        return team;
    }
}
