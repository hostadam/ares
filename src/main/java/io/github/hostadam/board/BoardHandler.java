package io.github.hostadam.board;

import io.github.hostadam.AresImpl;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.UnaryOperator;

@RequiredArgsConstructor
public class BoardHandler {

    @NonNull @Getter
    private final AresImpl ares;
    @Getter
    private BoardSettings settings;
    private BukkitTask updateTask;

    private final ConcurrentMap<UUID, Board> boards = new ConcurrentHashMap<>();

    public void compute(UnaryOperator<BoardSettings> operator) {
        boolean hasCurrentSetting = this.settings != null;

        this.settings = operator.apply(hasCurrentSetting ? this.settings : new BoardSettings());
        this.checkUpdateTask(hasCurrentSetting);
    }

    public void setupPlayer(Player player) {
        if(this.settings == null) return;
        if(this.settings.hasStyle()) {
            Board board = this.boards.computeIfAbsent(player.getUniqueId(), k -> new Board(player));
            board.construct();
            this.checkUpdateTask(false);
        }

        this.refreshPlayerOnTab(player);
    }

    public void destroyBoard(Player player) {
        Board board = this.boards.remove(player.getUniqueId());
        if(board != null) {
            board.destroy();
            this.checkUpdateTask(false);
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

    private int lastUpdatedTitleTick = 0;
    private int lastUpdatedTabTick = 0;

    private void checkUpdateTask(boolean wasSettingsChanged) {
        int interval = this.settings.getInterval();
        boolean empty = this.boards.isEmpty();

        if(this.updateTask != null && (empty || !this.settings.hasStyle() || wasSettingsChanged)) {
            this.updateTask.cancel();
            this.updateTask = null;
        } else if(this.updateTask == null && !empty && this.settings.hasStyle()) {
            this.updateTask = Bukkit.getScheduler().runTaskTimer(this.ares, this::tickAll, interval, interval);
        }
    }

    private void tickAll() {
        Component title = settings.shouldUpdateTitle(this.lastUpdatedTitleTick++) ? settings.getTitle() : null;

        boolean updateTab = settings.shouldUpdateTab(this.lastUpdatedTabTick++);
        Component tabHeader = updateTab ? settings.getTabHeader() : null;
        Component tabFooter = updateTab ? settings.getTabFooter() : null;

        for(Board board : this.boards.values()) {
            if(!board.shouldTick()) continue;

            board.updateLines(settings);
            board.updateTitleAndTab(title, tabHeader, tabFooter);
        }
    }
}
