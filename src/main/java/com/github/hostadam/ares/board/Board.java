package com.github.hostadam.ares.board;

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

    public void update() {
        if(this.scoreboard == null) return;
        BoardSettings settings = this.boardHandler.settings;
        BoardStyle style = settings.getStyle();
        if(style == null) return;

        this.objective.applyChanges(style.title(player), style.lines(player), settings);
        Component header = style.header(player), footer = style.footer(player);
        if(header != null) this.player.sendPlayerListHeader(header);
        if(footer != null) this.player.sendPlayerListFooter(footer);
    }
}
