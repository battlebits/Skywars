package br.com.battlebits.skywars.commands;

import org.bukkit.entity.Player;

import br.com.battlebits.commons.bukkit.command.BukkitCommandArgs;
import br.com.battlebits.commons.core.command.CommandClass;
import br.com.battlebits.commons.core.command.CommandFramework.Command;
import br.com.battlebits.commons.core.permission.Group;
import br.com.battlebits.commons.core.translate.T;
import br.com.battlebits.skywars.Main;
import br.com.battlebits.skywars.game.Engine;
import br.com.battlebits.skywars.game.GameStage;

public class StartCommand implements CommandClass {
    @Command(name = "start",
            usage = "</command>",
            groupToUse = Group.ADMIN,
            noPermMessageId = "skywars-command-start-no-permission")
    public void start(BukkitCommandArgs cmdArgs) {
        if (cmdArgs.isPlayer()) {
            String[] args = cmdArgs.getArgs();

            Player player = cmdArgs.getPlayer();

            if (args.length == 0) {
                Engine engine = Main.getInstance().getEngine();

                if (engine.getStage() == GameStage.PREGAME) {
                    engine.start();
                } else {
                    player.sendMessage(T.t(cmdArgs.getLanguage(), "skywars-command-start-ingame"));
                }
            } else {
                player.sendMessage(T.t(cmdArgs.getLanguage(), "skywars-command-start-usage"));
            }
        } else {
            cmdArgs.getSender().sendMessage(T.t(cmdArgs.getLanguage(), "skywars-command-non-console"));
        }
    }
}
