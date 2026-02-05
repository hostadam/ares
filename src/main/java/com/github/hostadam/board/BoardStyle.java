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

import io.papermc.paper.scoreboard.numbers.NumberFormat;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Function;

public class BoardStyle {

    /** Styling **/
    private Function<Player, Component> title, tabHeader, tabFooter;
    private Function<Player, List<Component>> lines;

    /** Updating intervals **/
    @Getter private int updateInterval = 5;
    @Getter private int secondaryUpdateInterval = 5;

    /** Design **/
    @Getter private NumberFormat scoreFormat = NumberFormat.noStyle();
    @Getter private Function<Player, Integer> tabListOrderingFunction;

    public BoardStyle() {}

    public boolean isValid() {
        return this.hasLines() || this.hasTab();
    }

    public boolean hasLines() {
        return this.title != null && this.lines != null;
    }

    public boolean hasTab() {
        return this.tabHeader != null || this.tabFooter != null;
    }

    public BoardStyle withTitle(@NotNull Function<Player, Component> title) {
        this.title = title;
        return this;
    }

    public BoardStyle withTabHeader(@NotNull Function<Player, Component> tabHeader) {
        this.tabHeader = tabHeader;
        return this;
    }

    public BoardStyle withTabFooter(@NotNull Function<Player, Component> tabFooter) {
        this.tabFooter = tabFooter;
        return this;
    }

    public BoardStyle withLines(@NotNull Function<Player, List<Component>> lines) {
        this.lines = lines;
        return this;
    }

    public BoardStyle withFormat(@NotNull NumberFormat format) {
        this.scoreFormat = format;
        return this;
    }

    public BoardStyle withUpdateInterval(int interval) {
        this.updateInterval = Math.max(1, interval);
        return this;
    }

    public BoardStyle withSecondaryUpdateInterval(int interval) {
        this.secondaryUpdateInterval = Math.max(1, interval);
        return this;
    }

    public BoardStyle withTablistOrdering(Function<Player, Integer> function) {
        this.tabListOrderingFunction = function;
        return this;
    }

    public void applyTabListOrder(Player player) {
        if(tabListOrderingFunction != null) player.setPlayerListOrder(this.tabListOrderingFunction.apply(player));
    }

    public Component createTitle(Player player) {
        return this.title != null ? this.title.apply(player) : null;
    }

    public Component createTabHeader(Player player) {
        return this.tabHeader != null ? this.tabHeader.apply(player) : null;
    }

    public Component createTabFooter(Player player) {
        return this.tabFooter != null ? this.tabFooter.apply(player) : null;
    }

    public List<Component> createLines(Player player) {
        return this.lines != null ? this.lines.apply(player) : null;
    }
}
