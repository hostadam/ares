package com.github.hostadam.ares;

import com.github.hostadam.ares.board.BoardHandler;
import com.github.hostadam.ares.chat.ChatListener;
import com.github.hostadam.ares.command.handler.CommandHandler;
import com.github.hostadam.ares.data.compat.AsyncBridge;
import com.github.hostadam.ares.menu.MenuListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public class AresImpl implements Ares {

    private AsyncBridge asyncBridge;
    private BoardHandler boardHandler;
    private CommandHandler commandHandler;

    public AresImpl(JavaPlugin owningPlugin) {
        this.init(owningPlugin);
    }

    private void init(JavaPlugin plugin) {
        this.boardHandler = new BoardHandler(plugin);
        this.commandHandler = new CommandHandler();
        this.asyncBridge = new AsyncBridge(plugin);

        PluginManager pluginManager = plugin.getServer().getPluginManager();
        pluginManager.registerEvents(this.boardHandler, plugin);
        pluginManager.registerEvents(new ChatListener(), plugin);
        pluginManager.registerEvents(new MenuListener(), plugin);

        Bukkit.getServicesManager().register(Ares.class, this, plugin, ServicePriority.Normal);
    }

    @Override
    public AsyncBridge async() {
        return this.asyncBridge;
    }

    @Override
    public BoardHandler scoreboard() {
        return this.boardHandler;
    }

    @Override
    public CommandHandler commands() {
        return this.commandHandler;
    }
}
