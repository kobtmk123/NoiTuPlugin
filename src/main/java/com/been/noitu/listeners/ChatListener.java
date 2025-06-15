package com.been.noitu.listeners;

import com.been.noitu.NoiTuPlugin;
import com.been.noitu.game.GameManager;
import com.been.noitu.game.GameSession;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {

    private final GameManager gameManager;

    public ChatListener(NoiTuPlugin plugin) {
        this.gameManager = plugin.getGameManager();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        GameSession session = gameManager.getPlayerGame(player.getUniqueId());

        if (session != null) {
            event.setCancelled(true);
            
             gameManager.getPlugin().getServer().getScheduler().runTask(gameManager.getPlugin(), () -> {
                session.processPlayerInput(player, event.getMessage());
            });
        }
    }
}