package com.been.noitu.game;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class GameSession {

    private final GameManager gameManager;
    private final UUID player1;
    private UUID player2; 
    private UUID currentPlayer;
    private String lastWord;
    private BukkitTask timerTask;

    public GameSession(GameManager gameManager, UUID player1) {
        this.gameManager = gameManager;
        this.player1 = player1;
        this.player2 = null;
    }

    public GameSession(GameManager gameManager, UUID player1, UUID player2) {
        this.gameManager = gameManager;
        this.player1 = player1;
        this.player2 = player2;
    }

    public void startGame() {
        if (isPvP()) {
            this.currentPlayer = ThreadLocalRandom.current().nextBoolean() ? player1 : player2;
             broadcastMessage("§aTrận đấu giữa " + Bukkit.getPlayer(player1).getName() + " và " + Bukkit.getPlayer(player2).getName() + " đã bắt đầu!");
        } else {
            this.currentPlayer = player1;
             broadcastMessage("§aBạn đã bắt đầu trận đấu với BOT!");
        }
       
        broadcastTitle("-- Nối Từ --");
        broadcastMessage("§e=> " + Bukkit.getPlayer(currentPlayer).getName() + " mời bạn ra từ trước!");
        startTurnTimer();
    }
    
    public void processPlayerInput(Player player, String word) {
        if (!player.getUniqueId().equals(currentPlayer)) {
            player.sendMessage("§cChưa đến lượt của bạn!");
            return;
        }

        word = word.trim().toLowerCase();
        
        if (!gameManager.getWordManager().isValidWord(word)) {
             player.sendMessage("§cTừ không hợp lệ hoặc không có trong từ điển!");
             return;
        }

        if (lastWord != null && !gameManager.getWordManager().isValidChain(lastWord, word)) {
            player.sendMessage("§cBạn nối từ sai rồi! Phải bắt đầu bằng âm '" + gameManager.getWordManager().getLastSyllable(lastWord) + "'");
            return;
        }

        if (timerTask != null) {
            timerTask.cancel();
        }

        this.lastWord = word;
        broadcastChat(player, word);

        switchTurn();
    }
    
    private void switchTurn() {
        if (isPvP()) {
            currentPlayer = currentPlayer.equals(player1) ? player2 : player1;
            Player nextPlayer = Bukkit.getPlayer(currentPlayer);
            if(nextPlayer == null || !nextPlayer.isOnline()){
                UUID winner = currentPlayer.equals(player1) ? player2 : player1;
                endGame(winner, currentPlayer);
                return;
            }
        } else {
            handleBotTurn();
            return;
        }

        startTurnTimer();
    }
    
    private void handleBotTurn() {
        String botWord = gameManager.getWordManager().findNextWord(lastWord);
        
        gameManager.getPlugin().getServer().getScheduler().runTaskLater(gameManager.getPlugin(), () -> {
            if (botWord == null) {
                broadcastTitle("-- Nối Từ --");
                broadcastMessage("§aBOT bí từ! Bạn đã thắng!");
                endGame(player1, null);
            } else {
                this.lastWord = botWord;
                broadcastChat(null, botWord); 
                this.currentPlayer = player1; 
                startTurnTimer();
            }
        }, 20L * ThreadLocalRandom.current().nextInt(1, 3));
    }


    private void startTurnTimer() {
        Player player = Bukkit.getPlayer(currentPlayer);
        if (player == null) return;
        
        timerTask = gameManager.getPlugin().getServer().getScheduler().runTaskLater(gameManager.getPlugin(), () -> {
             broadcastTitle("-- Nối Từ --");
             broadcastMessage("§c[" + player.getName() + " thua cuộc] vì hết giờ!");
             
             UUID winner = null;
             if(isPvP()){
                 winner = currentPlayer.equals(player1) ? player2 : player1;
             }
             
             endGame(winner, currentPlayer);
             
        }, 20L * 30); 
    }
    
    public void playerQuit(Player player) {
        broadcastTitle("-- Nối Từ --");
        broadcastMessage("§c[" + player.getName() + " đã bỏ cuộc]");
        UUID winner = null;
        if(isPvP()){
             winner = player.getUniqueId().equals(player1) ? player2 : player1;
        }
        endGame(winner, player.getUniqueId());
    }

    private void endGame(UUID winner, UUID loser) {
        gameManager.endGame(this, winner, loser);
    }

    private void broadcastMessage(String message) {
        Player p1 = Bukkit.getPlayer(player1);
        if (p1 != null) p1.sendMessage(message);

        if (isPvP()) {
            Player p2 = Bukkit.getPlayer(player2);
            if (p2 != null) p2.sendMessage(message);
        }
    }
    
    private void broadcastTitle(String title) {
        broadcastMessage("§6§l" + title);
    }
    
     private void broadcastChat(Player sender, String message) {
        String format;
        if (sender != null) { 
            String prefix = "§7[Nối Từ]§f ";
            format = prefix + sender.getDisplayName() + ": §e" + message;
        } else { 
             format = "§7[Nối Từ]§f §c[BOT]§r: §e" + message;
        }
        broadcastMessage(format);
    }

    public boolean isPvP() {
        return player2 != null;
    }

    public UUID getPlayer1() { return player1; }
    public UUID getPlayer2() { return player2; }
    public BukkitTask getTimerTask() { return timerTask; }
}