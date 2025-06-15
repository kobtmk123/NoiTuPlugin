package com.been.noitu.listeners;

import com.been.noitu.NoiTuPlugin;
import com.been.noitu.game.GameManager;
import com.been.noitu.game.GameSession;
import io.papermc.paper.event.player.AsyncChatEvent; // THAY ĐỔI IMPORT
import net.kyori.adventure.text.serializer.plain.PlainComponentSerializer; // IMPORT MỚI
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class ChatListener implements Listener {

    private final GameManager gameManager;

    public ChatListener(NoiTuPlugin plugin) {
        this.gameManager = plugin.getGameManager();
    }

    // THAY ĐỔI SỰ KIỆN TỪ AsyncPlayerChatEvent SANG AsyncChatEvent
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        GameSession session = gameManager.getPlayerGame(player.getUniqueId());

        if (session != null) {
            // Hủy tin nhắn gốc để nó không hiện ra chat thường
            event.setCancelled(true);

            // Lấy nội dung tin nhắn từ dạng "Component" sang dạng "String"
            String message = PlainComponentSerializer.plain().serialize(event.message());

            // Xử lý input trong thread chính để đảm bảo an toàn
            gameManager.getPlugin().getServer().getScheduler().runTask(gameManager.getPlugin(), () -> {
                session.processPlayerInput(player, message);
            });
        }
    }
}