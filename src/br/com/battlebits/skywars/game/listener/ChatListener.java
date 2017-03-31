package br.com.battlebits.skywars.game.listener;

import br.com.battlebits.skywars.game.Engine;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Iterator;

/**
 * Arquivo criado em 29/03/2017.
 * Desenvolvido por:
 *
 * @author Luãn Pereira.
 */
public class ChatListener implements Listener
{
    private Engine engine;

    public ChatListener(Engine engine)
    {
        this.engine = engine;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event)
    {
        Player player = event.getPlayer();

        if (!engine.contains(player))
        {
            Iterator<Player> iterator = event.getRecipients().iterator();
            event.setFormat("§7[SPECTATOR] " + player.getDisplayName() + " §7» §r%2$s");

            while (iterator.hasNext())
            {
                Player target = iterator.next();
                if (engine.contains(target))
                    iterator.remove();
            }
        }
    }
}
