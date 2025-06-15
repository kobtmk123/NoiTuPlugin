package com.been.noitu.game;

import com.been.noitu.NoiTuPlugin;
import com.been.noitu.utils.WordManager;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GameManager {

    private final NoiTuPlugin plugin;
    private final Map<UUID, GameSession> activeGames = new HashMap<>();
    private final Map<UUID, UUID> challengeRequests = new HashMap<>();
    private final WordManager wordManager;

    public GameManager(NoiTuPlugin plugin) {
        this.plugin = plugin;
        this.wordManager = new WordManager(plugin);
    }

    public void createPvEGame(Player player) {
        if (isInGame(player.getUniqueId())) {
            player.sendMessage("§cBạn đã ở trong một trận đấu rồi!");
            return;
        }
        GameSession session = new GameSession(this, player.getUniqueId());
        activeGames.put(player.getUniqueId(), session);
        session.startGame();
    }

    public void createChallenge(Player challenger, Player target) {
        if (isInGame(challenger.getUniqueId())) {
            challenger.sendMessage("§cBạn đang trong trận, không thể thách đấu!");
            return;
        }
        if (isInGame(target.getUniqueId())) {
            challenger.sendMessage("§c" + target.getName() + " đang trong trận khác.");
            return;
        }
        challengeRequests.put(challenger.getUniqueId(), target.getUniqueId());
        challenger.sendMessage("§aĐã gửi lời mời thách đấu tới " + target.getName() + ". Họ có 60 giây để chấp nhận.");
        target.sendMessage("§e" + challenger.getName() + " muốn thách đấu nối từ với bạn!");
        target.sendMessage("§aGõ /nt accept để chấp nhận hoặc /nt deny để từ chối.");

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (challengeRequests.containsKey(challenger.getUniqueId())) {
                challengeRequests.remove(challenger.getUniqueId());
                challenger.sendMessage("§cLời mời thách đấu tới " + target.getName() + " đã hết hạn.");
            }
        }, 20 * 60);
    }

    public void acceptChallenge(Player target) {
        UUID challengerUUID = null;
        for (Map.Entry<UUID, UUID> entry : challengeRequests.entrySet()) {
            if (entry.getValue().equals(target.getUniqueId())) {
                challengerUUID = entry.getKey();
                break;
            }
        }

        if (challengerUUID == null) {
            target.sendMessage("§cKhông có lời mời thách đấu nào đang chờ bạn.");
            return;
        }

        challengeRequests.remove(challengerUUID);

        Player challenger = plugin.getServer().getPlayer(challengerUUID);
        if (challenger == null || !challenger.isOnline()) {
            target.sendMessage("§cNgười thách đấu đã offline.");
            return;
        }

        GameSession session = new GameSession(this, challenger.getUniqueId(), target.getUniqueId());
        activeGames.put(challenger.getUniqueId(), session);
        activeGames.put(target.getUniqueId(), session);
        session.startGame();
    }
     public void denyChallenge(Player target) {
        UUID challengerUUID = null;
        for (Map.Entry<UUID, UUID> entry : challengeRequests.entrySet()) {
            if (entry.getValue().equals(target.getUniqueId())) {
                challengerUUID = entry.getKey();
                break;
            }
        }

        if (challengerUUID == null) {
            target.sendMessage("§cKhông có lời mời thách đấu nào đang chờ bạn.");
            return;
        }

        challengeRequests.remove(challengerUUID);
        target.sendMessage("§aBạn đã từ chối lời thách đấu.");
        Player challenger = plugin.getServer().getPlayer(challengerUUID);
        if (challenger != null && challenger.isOnline()) {
            challenger.sendMessage("§c" + target.getName() + " đã từ chối lời thách đấu của bạn.");
        }
    }


    public void endGame(GameSession session, UUID winner, UUID loser) {
        if(session.isPvP()){
            activeGames.remove(session.getPlayer1());
            activeGames.remove(session.getPlayer2());
        } else {
            activeGames.remove(session.getPlayer1());
        }
        
        if (session.getTimerTask() != null) {
            session.getTimerTask().cancel();
        }
    }

    public boolean isInGame(UUID playerUUID) {
        return activeGames.containsKey(playerUUID);
    }

    public GameSession getPlayerGame(UUID playerUUID) {
        return activeGames.get(playerUUID);
    }

    public NoiTuPlugin getPlugin() {
        return plugin;
    }

    public WordManager getWordManager() {
        return wordManager;
    }
}