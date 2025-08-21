package io.github.hostadam.board;

import io.github.hostadam.api.BoardStyle;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class Board {

    private final BoardHandler boardHandler;
    private final Player player;
    private final BoardObjective objective;

    @Getter
    private Scoreboard scoreboard;

    public Board(BoardHandler boardHandler, Player player) {
        this.boardHandler = boardHandler;
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
        this.lastUpdatedTitleTick = this.lastUpdatedTabTick = 0;
    }

    public void updateVisibility(boolean visible) {
        this.objective.setVisible(visible);
        this.lastUpdatedTitleTick = this.lastUpdatedTabTick = 0;
    }

    public void destroy() {
        if(this.scoreboard == null) return;
        this.scoreboard.getTeams().forEach(Team::unregister);
        this.scoreboard.getObjectives().forEach(Objective::unregister);
        this.lastUpdatedTitleTick = this.lastUpdatedTabTick = 0;
        this.scoreboard = null;
    }

    private int lastUpdatedTitleTick = 0, lastUpdatedTabTick = 0;

    public void update(BoardSettings settings) {
        if(this.scoreboard == null) return;
        BoardStyle style = settings.getStyle();

        if(settings.shouldUpdateTitle(this.lastUpdatedTitleTick++)) {
            this.objective.applyTitle(style.title(player));
        }

        if(settings.shouldUpdateTab(this.lastUpdatedTabTick++)) {
            Component header = style.header(player), footer = style.footer(player);
            if(header != null) this.player.sendPlayerListHeader(header);
            if(footer != null) this.player.sendPlayerListFooter(footer);
        }

        if(style.shouldUpdateLines(player)) {
            this.objective.applyChanges(style.lines(player), settings);
        }
    }
}
