package br.com.battlebits.skywars.game;

import br.com.battlebits.commons.core.data.DataServer;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import br.com.battlebits.commons.BattlebitsAPI;
import br.com.battlebits.commons.api.tablist.TabListAPI;
import br.com.battlebits.commons.api.title.TitleAPI;
import br.com.battlebits.commons.core.account.BattlePlayer;
import br.com.battlebits.commons.core.translate.T;
import br.com.battlebits.skywars.Main;
import br.com.battlebits.skywars.data.PlayerData;
import br.com.battlebits.skywars.utils.IFirework;
import br.com.battlebits.skywars.utils.Utils;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Wither;

public class GameSchedule implements Runnable {
    private Engine engine;

    @Getter
    @Setter
    private int time = 30, shutdown = 15;

    public static final int REFIL_1 = 420;
    public static final int REFIL_2 = 900;
    public static final int FINAL = 1200;

    public GameSchedule(Engine engine) {
        engine.setSchedule(this);
        this.engine = engine;
    }

    @Override
    public void run() {
        switch (engine.getStage()) {
            case PREGAME: {
                int count = engine.getPlayers().size();
                int maxcount = Bukkit.getMaxPlayers();
                int percent = (count * 100) / maxcount;

                if (percent >= 100 && time > 10) {
                    time = 10;
                }

                if (percent >= 80) {
                    if (time > 0) {
                        time--;

                        if (time > 0 && (time <= 5 || time == 10)) {
                            for (Player player : Bukkit.getOnlinePlayers()) {
                                player.playSound(player.getLocation(), Sound.NOTE_PLING, 1F, 3F);
                                player.sendMessage(T.t(BattlePlayer.getLanguage(player.getUniqueId()), "skywars-time-to-start", new String[]{"%timeLeft%", "%complement%"}, new String[]{Integer.toString(time), time != 1 ? "s!" : "!"}));
                            }
                        }
                    } else {
                        engine.start();
                    }
                } else {
                    time = 30;
                }

                break;
            }

            case PREPARING: {
                if (time > 0) {
                    time--;
                    engine.getCages().forEach(v -> v.run(time));
                } else {
                    engine.setStage(GameStage.INGAME);
                    engine.getCages().clear();
                }

                break;
            }

            case INGAME: {
                time++;

                switch (time) {
                    case REFIL_1:
                    case REFIL_2: {
                        engine.applyRefill("feast", "feast-l2");
                        engine.applyRefill("subfeast", "subfeast-l2");
                        engine.applyRefill("player-1", "player-1-l2");
                        engine.applyRefill("player-2", "player-2-l2");
                        engine.applyRefill("player-3", "player-3-l2");

                        for (Player player : Bukkit.getOnlinePlayers()) {
                            TitleAPI.setTitle(player, " ", T.t(BattlePlayer.getLanguage(player.getUniqueId()), "skywars-chests-refilled-title"), 10, 100, 10, true);
                            player.playSound(player.getLocation(), Sound.CHEST_OPEN, 1F, 1.1F);
                        }

                        break;
                    }

                    case FINAL: {
                        Location location = engine.getMap().getSpawn("spectators");

                        for (int k = 1; k <= 3; k++) {
                            Wither wither = location.getWorld().spawn(location.clone(), Wither.class);
                            wither.setNoDamageTicks(Integer.MAX_VALUE);
                            wither.setCustomNameVisible(false);
                            wither.setCustomName(" ");
                            wither.setMaxHealth(2048D);
                            wither.setHealth(2048D);
                        }

                        break;
                    }
                }

                break;
            }

            case ENDING: {
                if (shutdown > 0) {
                    shutdown--;

                    for (Player player : engine.getPlayers()) {
                        Color color1 = Utils.getRandomColor();
                        Color color2 = Utils.getRandomColor();

                        for (int i = 2; i <= 8; i++) {
                            double raio = Math.toRadians(360 / 7 * i);
                            double x = 4 * Math.cos(raio);
                            double z = 4 * Math.sin(raio);
                            Location loc = player.getLocation().clone().add(x, 0.2D, z);
                            IFirework.spawn(loc, FireworkEffect.builder().with(Type.BALL).withColor(color1).withFade(color2).build());
                        }
                    }
                } else {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        ByteArrayDataOutput out = ByteStreams.newDataOutput();
                        out.writeUTF("Lobby");
                        player.sendPluginMessage(Main.getInstance(), "BungeeCord", out.toByteArray());
                    }

                    Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> Bukkit.shutdown(), 60L);
                }

                break;
            }
        }

        Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () -> DataServer.updateStatus(engine.getStage().toMinigameState(), time));
        updateTabHeaderAndFooter();
        updateVanished();
    }

    private void updateVanished() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            for (Player other : Bukkit.getOnlinePlayers()) {
                if (!other.equals(player)) {
                    if (!engine.contains(other)) {
                        if (player.canSee(other)) {
                            player.hidePlayer(other);
                        }
                    } else if (!player.canSee(other)) {
                        player.showPlayer(other);
                    }
                }
            }
        }
    }

    private void updateTabHeaderAndFooter() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerData pd = Main.getInstance().getPlayerManager().get(player);

            BattlePlayer bp = BattlebitsAPI.getAccountCommon().getBattlePlayer(player.getUniqueId());

            if (pd != null && bp != null) {
                TabListAPI.setHeaderAndFooter(player, " \n§e" + time(engine.getSchedule().getTime()) + " §6> Servidor: §f" + Bukkit.getServerName() + " §6< §e" + engine.getPlayers().size() + "/" + Bukkit.getMaxPlayers() + "\n§6Mapa: §e" + engine.getMap().getName() + " §1- §6Kit: §eNenhum §1- §6Kills: §e" + engine.getKills(player) + " §1- §6Ping: §e" + ((CraftPlayer) player).getHandle().ping + "\n ", "\n§bNick: §f" + player.getName() + " §1- §bWins: §f" + pd.getWins() + " §1- §bLiga: §f" + bp.getLeague().name() + "\n§bMais informações: §fbattlebits.com.br\n ");
            }
        }
    }

    private String time(int time) {
        return time / 60 + ":" + (time % 60 < 10 ? "0" : "") + time % 60;
    }
}
