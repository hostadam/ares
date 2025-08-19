package io.github.hostadam.board;

import io.github.hostadam.api.BoardStyle;
import io.papermc.paper.scoreboard.numbers.NumberFormat;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

@Getter
public class BoardSettings {

    private int interval = 5;
    private Integer tabInterval = null;
    private Integer titleInterval = null;
    private BoardStyle style;
    private NumberFormat scoreFormat = NumberFormat.noStyle();
    private Function<Player, Integer> tabListOrdering;

    public BoardSettings() {}

    public BoardSettings scoreFormat(@NotNull NumberFormat format) {
        this.scoreFormat = format;
        return this;
    }

    public BoardSettings interval(int interval) {
        this.interval = Math.max(1, interval);
        return this;
    }

    public BoardSettings tabInterval(int interval) {
        this.tabInterval = interval;
        return this;
    }

    public BoardSettings neverUpdateTab() {
        this.tabInterval = Integer.MAX_VALUE;
        return this;
    }

    public BoardSettings titleInterval(int interval) {
        this.titleInterval = interval;
        return this;
    }

    public BoardSettings neverUpdateTitle() {
        this.titleInterval = Integer.MAX_VALUE;
        return this;
    }

    public BoardSettings style(BoardStyle style) {
        this.style = style;
        return this;
    }

    public BoardSettings tabListOrdering(Function<Player, Integer> function) {
        this.tabListOrdering = function;
        return this;
    }

    public boolean shouldUpdateTitle(int currentTick) {
        return this.titleInterval == null || currentTick == 0 || (this.titleInterval != Integer.MAX_VALUE && currentTick % titleInterval == 0);
    }

    public boolean shouldUpdateTab(int currentTick) {
        return this.tabInterval == null || currentTick == 0 || (this.tabInterval != Integer.MAX_VALUE && currentTick % tabInterval == 0);
    }

    public void applyTabListOrder(Player player) {
        player.setPlayerListOrder(this.tabListOrdering.apply(player));
    }

    public boolean hasStyle() {
        return this.style != null;
    }
}
