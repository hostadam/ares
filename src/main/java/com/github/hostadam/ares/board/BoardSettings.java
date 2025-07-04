package com.github.hostadam.ares.board;

import io.papermc.paper.scoreboard.numbers.NumberFormat;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class BoardSettings {

    @Getter
    private int interval = 5;
    @Getter
    private BoardStyle style;
    @Getter
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

    public BoardSettings style(BoardStyle style) {
        this.style = style;
        return this;
    }

    public BoardSettings tabListOrdering(Function<Player, Integer> function) {
        this.tabListOrdering = function;
        return this;
    }

    public void applyTabListOrder(Player player) {
        if(this.tabListOrdering != null) {
            player.setPlayerListOrder(this.tabListOrdering.apply(player));
        }
    }

    public boolean hasStyle() {
        return this.style != null;
    }
}
