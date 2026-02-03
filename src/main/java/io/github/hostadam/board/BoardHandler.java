package io.github.hostadam.board;

import io.github.hostadam.AresPlugin;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.math.BigInteger;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.UnaryOperator;

@RequiredArgsConstructor
public class BoardHandler {

    @NonNull
    private final AresPlugin ares;

    @Getter
    private BoardStyle style;
    private BukkitTask updateTask;

    private final AtomicInteger ticker = new AtomicInteger();
    private final ConcurrentMap<UUID, Board> boards = new ConcurrentHashMap<>();

    private Optional<Board> get(UUID uniqueId) {
        return Optional.ofNullable(this.boards.get(uniqueId));
    }

    public Optional<Board> get(Player player) {
        return get(player.getUniqueId());
    }

    public void setStyle(UnaryOperator<BoardStyle> operator) {
        boolean hasCurrentSetting = this.style != null;
        this.style = operator.apply(hasCurrentSetting ? this.style : new BoardStyle());
        this.checkUpdateTask();
    }

    public void refreshPlayerOrder(Player player) {
        this.style.applyTabListOrder(player);
    }

    public void destroyBoard(Player player) {
        Board board = this.boards.remove(player.getUniqueId());
        if(board == null) return;
        board.destroy();
        this.checkUpdateTask();
    }

    public void handlePlayerJoin(Player player) {
        if(style == null) return;
        if(this.style.isValid()) {
            Board board = this.boards.computeIfAbsent(player.getUniqueId(), k -> new Board(this, player));
            board.setVisible(true);
            player.setScoreboard(board.getScoreboard());

            board.updateLines();
            board.updateTitleAndTab(Bukkit.getCurrentTick());

            this.checkUpdateTask();
        }

        style.applyTabListOrder(player);
    }

    private void checkUpdateTask() {
        if(this.updateTask != null) {
            this.updateTask.cancel();
            this.updateTask = null;
        } else {
            this.updateTask = Bukkit.getScheduler().runTaskTimer(this.ares, () -> {
                int currentTick = this.ticker.incrementAndGet();

                for(Board board : this.boards.values()) {
                    if(!board.shouldTick()) continue;
                    board.updateLines();
                    board.updateTitleAndTab(currentTick);
                }
            }, this.style.getUpdateInterval(), this.style.getUpdateInterval());
        }
    }
}
