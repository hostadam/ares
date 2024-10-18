package com.github.hostadam.board;

import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BoardHandler implements Listener {

    private final JavaPlugin plugin;
    @Setter
    private BoardAdapter adapter;
    private final Map<UUID, Board> boards;

    public BoardHandler(JavaPlugin plugin) {
        this.plugin = plugin;
        this.boards = new HashMap<>();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public Board getScoreboard(UUID uniqueId) {
        return this.boards.get(uniqueId);
    }

    public void refresh() {
        this.boards.values().forEach(board -> {
            board.setAdapter(this.adapter);
            board.update();
        });
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if(this.adapter == null) return;

        Player player = event.getPlayer();
        this.boards.put(player.getUniqueId(), new Board(this.plugin, player, this.adapter));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Board board = this.boards.remove(player.getUniqueId());
        if(board != null) {
            if(board.getNametagHandler() != null && board.getNametagHandler() instanceof Listener listener) {
                HandlerList.unregisterAll(listener);
            }

            board.remove();
        }
    }
}
