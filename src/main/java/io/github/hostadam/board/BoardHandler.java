package io.github.hostadam.board;

import io.github.hostadam.AresImpl;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

@RequiredArgsConstructor
public class BoardHandler {

    @NonNull
    private final AresImpl ares;
    @Getter
    private BoardSettings settings;
    private BukkitTask updateTask;

    private final ConcurrentMap<UUID, Board> boards = new ConcurrentHashMap<>();

    public void updateSettings(BoardSettings settings) {
        this.settings = settings;
        this.checkUpdateTask();
    }

    public void setupPlayer(Player player) {
        if(this.settings == null) return;
        if(this.settings.hasStyle()) {
            Board board = this.boards.computeIfAbsent(player.getUniqueId(), k -> new Board(this, player));
            board.construct();
            this.checkUpdateTask();
        }

        this.refreshPlayerOnTab(player);
    }

    public void destroyBoard(Player player) {
        Board board = this.boards.remove(player.getUniqueId());
        if(board != null) {
            board.destroy();
            this.checkUpdateTask();
        }
    }

    private Optional<Board> get(UUID uniqueId) {
        return Optional.ofNullable(this.boards.get(uniqueId));
    }

    public Optional<Board> get(Player player) {
        return get(player.getUniqueId());
    }

    public void refreshPlayerOnTab(Player player) {
        if(this.settings != null && settings.getTabListOrdering() != null) {
            settings.applyTabListOrder(player);
        }
    }

    private void checkUpdateTask() {
        int interval = this.settings.getInterval();
        boolean empty = this.boards.isEmpty();
        if(this.updateTask != null && (empty || !this.settings.hasStyle())) {
            this.updateTask.cancel();
            this.updateTask = null;
        } else if(this.updateTask == null && !empty && this.settings.hasStyle()) {
            this.updateTask = Bukkit.getScheduler().runTaskTimer(this.ares, () -> {
                for(Board board : this.boards.values()) {
                    if(board.shouldTick()) board.update();
                }
            }, interval, interval);
        }
    }
}
