package com.been.noitu;

import com.been.noitu.commands.NoiTuCommand;
import com.been.noitu.game.GameManager;
import com.been.noitu.listeners.ChatListener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;

public final class NoiTuPlugin extends JavaPlugin {

    private static NoiTuPlugin instance;
    private GameManager gameManager;

    @Override
    public void onEnable() {
        instance = this;
        this.gameManager = new GameManager(this);

        try {
            gameManager.getWordManager().loadWords();
        } catch (IOException e) {
            getLogger().severe("Không thể tải file words.txt! Plugin sẽ không hoạt động đúng.");
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getCommand("nt").setExecutor(new NoiTuCommand(this));
        getCommand("thachdau").setExecutor(new NoiTuCommand(this));

        getServer().getPluginManager().registerEvents(new ChatListener(this), this);

        getLogger().info("NoiTuPlugin đã được bật!");
    }

    @Override
    public void onDisable() {
        getLogger().info("NoiTuPlugin đã được tắt!");
    }

    public static NoiTuPlugin getInstance() {
        return instance;
    }

    public GameManager getGameManager() {
        return gameManager;
    }
}