package br.com.battlebits.skywars.game.listener;

import br.com.battlebits.commons.api.bossbar.BossBarAPI;
import br.com.battlebits.commons.bukkit.event.update.UpdateEvent;
import br.com.battlebits.commons.bukkit.scoreboard.BattleBoard;
import br.com.battlebits.commons.util.string.AnimatedString;
import br.com.battlebits.skywars.Main;
import br.com.battlebits.skywars.data.PlayerData;
import br.com.battlebits.skywars.game.Engine;
import br.com.battlebits.skywars.game.GameSchedule;
import br.com.battlebits.skywars.game.modes.Team;
import br.com.battlebits.skywars.utils.NameTag;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scoreboard.Objective;

import java.util.HashMap;
import java.util.Map;

/**
 * Arquivo criado em 29/03/2017.
 * Desenvolvido por:
 *
 * @author Luãn Pereira.
 */
public class UpdateListener implements Listener {
    private Engine engine;
    private AnimatedString bossAnimated;
    private AnimatedString titleAnimated;

    public UpdateListener(Engine engine) {
        this.engine = engine;
        this.bossAnimated = new AnimatedString("BATTLEBITS.COM.BR", "§6§l", "§e§l", "§7§l", 0);
        this.titleAnimated = new AnimatedString("SKYWARS", "§6§l", "§e§l", "§7§l", 0);
    }

    @EventHandler
    public void onUpdate(UpdateEvent event) {
        switch (event.getType()) {
            case TICK: {
                if (event.getCurrentTick() % 2 > 0)
                    break;

                String nextBoss = bossAnimated.next();
                String nextTitle = titleAnimated.next();

                for (Player player : Bukkit.getOnlinePlayers()) {
                    BossBarAPI.setBar(player, nextBoss, 100F);
                    PlayerData playerData = Main.getInstance().getPlayerManager().get(player);

                    if (playerData != null) {
                        BattleBoard bboard = playerData.getBattleBoard();
                        bboard.setDisplayName(nextTitle);
                    }
                }

                break;
            }

            case SECOND: {
                Map<Integer, String> rows = new HashMap<>();

                for (Player player : Bukkit.getOnlinePlayers()) {
                    PlayerData playerData = Main.getInstance().getPlayerManager().get(player);

                    if (playerData != null) {
                        BattleBoard board = playerData.getBattleBoard();

                        switch (engine.getStage()) {
                            case PREGAME: {
                                int count = engine.getPlayers().size();
                                rows.put(15 - rows.size(), " ");

                                if ((count * 100) / Bukkit.getMaxPlayers() >= 80)
                                    rows.put(15 - rows.size(), "Começa em: §a" + time(engine.getSchedule().getTime()));
                                else {
                                    StringBuilder builder = new StringBuilder();
                                    long waiting = (event.getCurrentTick() / 20) % 4;
                                    for (int i = 0; i < waiting; i++)
                                        builder.append(".");
                                    rows.put(15 - rows.size(), "Aguardando§a" + builder.toString());
                                }

                                rows.put(15 - rows.size(), "Jogadores: §a" + count + "/" + Bukkit.getMaxPlayers());

                                break;
                            }

                            case PREPARING: {
                                rows.put(15 - rows.size(), " ");
                                rows.put(15 - rows.size(), "Abrindo em: §a" + time(engine.getSchedule().getTime()));
                                rows.put(15 - rows.size(), "Jogadores: §a" + engine.getPlayers().size() + "/" + Bukkit.getMaxPlayers());
                                break;
                            }

                            default: {
                                rows.put(15 - rows.size(), " ");

                                int time = engine.getSchedule().getTime();
                                Objective belowName = board.registerBelowName("§c\u2764");
                                Objective playerList = board.registerPlayerList();

                                for (Player target : Bukkit.getOnlinePlayers()) {
                                    int score = !engine.contains(target) ? 0 : Math.max(1, (int) target.getHealth());
                                    belowName.getScore(target.getName()).setScore(score);
                                    playerList.getScore(target.getName()).setScore(score);
                                }

                                if (time < GameSchedule.FINAL) {
                                    rows.put(15 - rows.size(), "Próximo evento:");

                                    if (time < GameSchedule.REFIL_2)
                                        rows.put(15 - rows.size(), "§aRefil " + time(time > GameSchedule.REFIL_1 ? (GameSchedule.REFIL_2 - time) : (GameSchedule.REFIL_1 - time)));
                                    else
                                        rows.put(15 - rows.size(), "§aFinal em " + time(GameSchedule.FINAL - time));

                                    rows.put(15 - rows.size(), " ");
                                }

                                rows.put(15 - rows.size(), "Kills: §a" + engine.getKills(player));
                                rows.put(15 - rows.size(), "Vivos: §a" + engine.getPlayers().size());

                                if (engine instanceof Team)
                                    rows.put(15 - rows.size(), "Times: §a" + engine.getTeamCount());

                                rows.put(15 - rows.size(), " ");

                                String str1 = (engine.isInsane() ? "§c" : "§a");
                                String str2 = (engine.isInsane() ? " " + str1 + "§lINSANO" : "");

                                rows.put(15 - rows.size(), "Modo: §a" + str1 + engine.getType().getName() + str2);
                                rows.put(15 - rows.size(), "Mapa: §a" + engine.getMap().getName());
                                break;
                            }
                        }

                        rows.put(15 - rows.size(), " ");
                        rows.put(15 - rows.size(), "§ebattlebits.com.br");
                        board.setRows(rows);
                        rows.clear();

                        NameTag nameTag = playerData.getNameTag();
                        if (nameTag != null)
                            nameTag.update();
                    }
                }

                break;
            }
        }
    }

    private String time(int time) {
        return time / 60 + ":" + (time % 60 < 10 ? "0" : "") + time % 60;
    }
}
