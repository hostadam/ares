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

import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.List;

public class Board {

    private final BoardHandler boardHandler;
    private final Player player;

    private BoardObjective objective;
    @Getter private Scoreboard scoreboard;

    private int lastSecondaryTick = -1;

    public Board(BoardHandler boardHandler, Player player) {
        this.boardHandler = boardHandler;
        this.player = player;
        this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        this.objective = new BoardObjective(this.scoreboard);
    }

    public boolean shouldTick() {
        return this.player != null && this.scoreboard != null && this.objective.isVisible();
    }

    public void setVisible(boolean visible) {
        this.objective.setVisible(visible);
    }

    public void destroy() {
        if(this.scoreboard == null) return;
        this.scoreboard.getTeams().forEach(Team::unregister);
        this.scoreboard.getObjectives().forEach(Objective::unregister);
        this.scoreboard = null;
        this.objective = null;
    }

    public void updateTitleAndTab(int currentTick) {
        BoardStyle style = this.boardHandler.getStyle();
        if(this.lastSecondaryTick == -1 || currentTick - this.lastSecondaryTick >= style.getSecondaryUpdateInterval()) {
            Component title = style.createTitle(player);
            if(title != null) this.objective.setTitle(title);

            Component header = style.createTabHeader(player), footer = style.createTabFooter(player);
            if(header != null && footer != null) {
                this.player.sendPlayerListHeaderAndFooter(header, footer);
            } else if(header != null) {
                this.player.sendPlayerListHeader(header);
            } else if(footer != null) {
                this.player.sendPlayerListFooter(footer);
            }

            this.lastSecondaryTick = currentTick;
        }
    }

    public void updateLines() {
        BoardStyle style = this.boardHandler.getStyle();
        List<Component> lines = style.createLines(player);
        if(lines != null) {
            this.objective.setLines(lines, style.getScoreFormat());
        }
    }
}
