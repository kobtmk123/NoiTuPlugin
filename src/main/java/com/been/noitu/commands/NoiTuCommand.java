package com.been.noitu.commands;

import com.been.noitu.NoiTuPlugin;
import com.been.noitu.game.GameManager;
import com.been.noitu.game.GameSession;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class NoiTuCommand implements CommandExecutor {

    private final NoiTuPlugin plugin;
    private final GameManager gameManager;

    public NoiTuCommand(NoiTuPlugin plugin) {
        this.plugin = plugin;
        this.gameManager = plugin.getGameManager();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Lệnh này chỉ dành cho người chơi.");
            return true;
        }
        Player player = (Player) sender;

        if (command.getName().equalsIgnoreCase("thachdau")) {
            handleThachDau(player, args);
            return true;
        }

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();
        switch (subCommand) {
            case "help":
                sendHelp(player);
                break;
            case "play":
                gameManager.createPvEGame(player);
                break;
            case "bocuoc":
                 GameSession session = gameManager.getPlayerGame(player.getUniqueId());
                 if (session == null) {
                     player.sendMessage("§cBạn không ở trong trận đấu nào để bỏ cuộc.");
                 } else {
                     session.playerQuit(player);
                 }
                break;
            case "accept":
                 gameManager.acceptChallenge(player);
                break;
             case "deny":
                 gameManager.denyChallenge(player);
                break;
            default:
                player.sendMessage("§cLệnh không hợp lệ. Dùng /nt help để xem danh sách lệnh.");
                break;
        }

        return true;
    }
    
    private void handleThachDau(Player player, String[] args) {
        if(args.length == 0){
            player.sendMessage("§cCách dùng: /thachdau <tên người chơi>");
            return;
        }
        
        Player target = Bukkit.getPlayer(args[0]);
        if(target == null || !target.isOnline()){
            player.sendMessage("§cNgười chơi " + args[0] + " không online hoặc không tồn tại.");
            return;
        }
        if(target.equals(player)){
            player.sendMessage("§cBạn không thể thách đấu chính mình!");
            return;
        }
        
        gameManager.createChallenge(player, target);
    }
    
    private void sendHelp(Player player) {
        player.sendMessage("§6§l--- Hướng dẫn chơi Nối Từ ---");
        player.sendMessage("§e/nt play: §fChơi nối từ với BOT.");
        player.sendMessage("§e/thachdau <tên>: §fThách đấu với người chơi khác.");
        player.sendMessage("§e/nt accept: §fChấp nhận lời thách đấu.");
        player.sendMessage("§e/nt deny: §fTừ chối lời thách đấu.");
        player.sendMessage("§e/nt bocuoc: §fBỏ cuộc trận đấu hiện tại.");
        player.sendMessage("§e/nt help: §fHiển thị bảng giúp đỡ này.");
        if(player.isOp()){
            player.sendMessage("§c/nt reload: §7(Admin) Tải lại file từ điển.");
        }
    }
}