package br.com.battlebits.skywars.game.listener;

import br.com.battlebits.commons.api.bossbar.BossBarAPI;
import br.com.battlebits.commons.bukkit.event.update.UpdateEvent;
import br.com.battlebits.commons.bukkit.scoreboard.BattleBoard;
import br.com.battlebits.commons.util.string.AnimatedString;
import br.com.battlebits.skywars.Main;
import br.com.battlebits.skywars.data.PlayerData;
import br.com.battlebits.skywars.game.Engine;
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
public class UpdateListener implements Listener
{
    private Engine engine;
    private AnimatedString bossAnimated;
    private AnimatedString titleAnimated;

    public UpdateListener(Engine engine)
    {
        this.engine = engine;
        this.bossAnimated = new AnimatedString("BATTLEBITS.COM.BR", "§6§l", "§e§l", "§7§l", 0);
        this.titleAnimated = new AnimatedString("SKYWARS", "§6§l", "§e§l", "§7§l", 0);
    }

    @EventHandler
    public void onUpdate(UpdateEvent event)
    {
        switch (event.getType())
        {
            case TICK:
            {
                if (event.getCurrentTick() % 2 > 0)
                    break;

                String nextBoss = bossAnimated.next();
                String nextTitle = titleAnimated.next();

                for (Player player : Bukkit.getOnlinePlayers())
                {
                    BossBarAPI.setBar(player, nextBoss, 100F);
                    PlayerData playerData = Main.getInstance().getPlayerManager().get(player);

                    if (playerData != null)
                    {
                        BattleBoard bboard = playerData.getBattleBoard();
                        bboard.setDisplayName(nextTitle);
                    }
                }

                break;
            }

            case SECOND:
            {
                Map<Integer, String> rows = new HashMap<>();

                for (Player player : Bukkit.getOnlinePlayers())
                {
                    PlayerData playerData = Main.getInstance().getPlayerManager().get(player);

                    if (playerData != null)
                    {
                        BattleBoard bboard = playerData.getBattleBoard();

                        switch (engine.getStage())
                        {
                            case PREGAME:
                            {
                                break;
                            }

                            case PREPARING:
                            {
                                break;
                            }

                            default:
                            {
                                Objective belowName = bboard.registerBelowName("§c\u2764");
                                Objective playerList = bboard.registerPlayerList();

                                for (Player target : Bukkit.getOnlinePlayers())
                                {
                                    int score = !engine.contains(target) ? 0 : Math.max(1, (int) target.getHealth());
                                    belowName.getScore(target.getName()).setScore(score);
                                    playerList.getScore(target.getName()).setScore(score);
                                }

                                break;
                            }
                        }

                        rows.put(15 - rows.size(), " ");
                        rows.put(15 - rows.size(), "§7TYPE: §e" + engine.getType().name());
                        rows.put(15 - rows.size(), "§7STAGE: §e" + engine.getStage().name());
                        rows.put(15 - rows.size(), "§7INSANE: §e" + engine.isInsane());
                        rows.put(15 - rows.size(), " ");
                        rows.put(15 - rows.size(), "TODO");
                        rows.put(15 - rows.size(), " ");
                        rows.put(15 - rows.size(), "§ebattlebits.com.br");
                        bboard.setRows(rows);
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
}
