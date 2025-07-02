package com.github.hostadam.ares.board;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

@RequiredArgsConstructor
public class BoardHandler implements Listener {

    @NonNull
    protected final JavaPlugin plugin;
    protected BoardSettings settings;

    private BukkitTask updateTask;
    private final ConcurrentMap<UUID, Board> boards = new ConcurrentHashMap<>();

    public void updateSettings(BoardSettings settings) {
        this.settings = settings;
        this.checkUpdateTask();
    }

    private Optional<Board> get(UUID uniqueId) {
        return Optional.ofNullable(this.boards.get(uniqueId));
    }

    public Optional<Board> get(Player player) {
        return get(player.getUniqueId());
    }

    public Board getOrElseCreate(Player player) {
        return this.boards.computeIfAbsent(player.getUniqueId(), k -> new Board(this, player));
    }

    private void checkUpdateTask() {
        int interval = this.settings.getInterval();
        boolean empty = this.boards.isEmpty();
        if(this.updateTask == null && !empty) {
            this.updateTask = Bukkit.getScheduler().runTaskTimer(this.plugin, () -> this.boards.values().stream().filter(Board::shouldTick).forEach(Board::update), interval, interval);
        } else if(this.updateTask != null && (empty || !this.settings.hasStyle())) {
            this.updateTask.cancel();
            this.updateTask = null;
        }
    }

    private void handlePlayerJoin(Player player) {
        if(this.settings == null) return;
        if(this.settings.hasStyle()) {
            Board board = this.getOrElseCreate(player);
            board.construct();
            this.checkUpdateTask();
        }

        settings.applyTabListOrder(player);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        this.handlePlayerJoin(player);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Board board = this.boards.remove(player.getUniqueId());
        if(board != null) {
            board.destroy();
            this.checkUpdateTask();
        }
    }
}
