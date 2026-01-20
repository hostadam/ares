package io.github.hostadam.implementation.board;

import io.github.hostadam.api.BoardStyle;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class Board {

    private final Player player;
    private final BoardObjective objective;

    @Getter
    private Scoreboard scoreboard;

    public Board(Player player) {
        this.player = player;
        this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        this.objective = new BoardObjective(this.scoreboard);
    }

    public boolean shouldTick() {
        return this.player != null && this.scoreboard != null && this.objective.isVisible();
    }

    public void construct() {
        this.updateVisibility(true);
        player.setScoreboard(this.scoreboard);
    }

    public void updateVisibility(boolean visible) {
        this.objective.setVisible(visible);
    }

    public void destroy() {
        if(this.scoreboard == null) return;
        this.scoreboard.getTeams().forEach(Team::unregister);
        this.scoreboard.getObjectives().forEach(Objective::unregister);
        this.scoreboard = null;
    }

    public void updateLines(BoardSettings settings) {
        BoardStyle style = settings.getStyle();
        if(!style.shouldUpdateLines(player)) return;
        this.objective.applyChanges(style.lines(player), settings);
    }

    public void updateTitleAndTab(Component title, Component header, Component footer) {
        if(title != null) {
            this.objective.applyTitle(title);
        }

        this.updateTab(header, footer);
    }

    private void updateTab(Component header, Component footer) {
        boolean hasHeader = header != null, hasFooter = footer != null;

        if(hasHeader && hasFooter) {
            this.player.sendPlayerListHeaderAndFooter(header, footer);
        } else if(hasHeader) {
            this.player.sendPlayerListHeader(header);
        } else if(hasFooter) {
            this.player.sendPlayerListFooter(footer);
        }
    }
}
