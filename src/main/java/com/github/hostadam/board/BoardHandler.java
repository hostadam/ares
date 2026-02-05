/*
 * MIT License
 * Copyright (c) 2026 Hostadam
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.hostadam.board;

import com.github.hostadam.AresPlugin;
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
