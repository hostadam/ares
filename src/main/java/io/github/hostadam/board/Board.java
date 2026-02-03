package io.github.hostadam.board;

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
