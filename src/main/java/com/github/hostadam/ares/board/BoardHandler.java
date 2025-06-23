package com.github.hostadam.ares.board;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@RequiredArgsConstructor
public class BoardHandler implements Listener {

    @NonNull
    private final JavaPlugin plugin;
    private BoardAdapter adapter;
    private final ConcurrentMap<UUID, Board> boards = new ConcurrentHashMap<>();

    public void setAdapter(BoardAdapter adapter) {
        this.adapter = adapter;
        this.refresh();
    }

    public Board getScoreboard(Player player) {
        return this.getScoreboard(player.getUniqueId());
    }

    public Board getScoreboard(UUID uniqueId) {
        return this.boards.get(uniqueId);
    }

    public void refresh() {
        this.boards.values().forEach(board -> board.setAdapter(this.adapter));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent event) {
        if(this.adapter == null) return;
        Player player = event.getPlayer();
        this.boards.put(player.getUniqueId(), new Board(this.plugin, player, this.adapter));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Board board = this.boards.remove(player.getUniqueId());
        if(board != null) board.remove();
    }
}
