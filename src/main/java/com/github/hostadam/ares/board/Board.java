package com.github.hostadam.ares.board;

import com.github.hostadam.ares.board.nametag.NametagHandler;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class Board {

    private final JavaPlugin plugin;
    private final Player player;
    private final BoardObjective objective;

    private BoardAdapter adapter;
    private BukkitTask updateTask;

    @Getter
    private Scoreboard scoreboard;
    @Getter
    private boolean sidebarVisibility = true;
    @Getter @Setter
    private NametagHandler nametagHandler;

    public Board(JavaPlugin plugin, Player player, BoardAdapter adapter) {
        this.plugin = plugin;
        this.player = player;
        this.adapter = adapter;
        this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        this.objective = new BoardObjective(this.scoreboard);

        this.updateVisibility(true);
        player.setScoreboard(this.scoreboard);
    }

    public void setAdapter(BoardAdapter adapter) {
        this.adapter = adapter;
        this.update();
    }

    public void setTab(Component header, Component footer) {
        this.player.sendPlayerListHeaderAndFooter(header, footer);
    }

    public void remove() {
        if(this.nametagHandler != null) {
            this.nametagHandler.shutdown();

            if(this.nametagHandler instanceof Listener listener) HandlerList.unregisterAll(listener);
        }

        if(this.updateTask != null) {
            this.updateTask.cancel();
            this.updateTask = null;
        }

        if(this.scoreboard != null) {
            this.scoreboard.getTeams().forEach(Team::unregister);
            this.scoreboard.getObjectives().forEach(Objective::unregister);
            this.scoreboard = null;
        }
    }

    public void updateVisibility(boolean visible) {
        if(visible && this.updateTask == null) {
            this.initialize();
        } else if(!visible && this.updateTask != null) {
            this.updateTask.cancel();
            this.updateTask = null;
        }

        this.sidebarVisibility = visible;
        this.objective.setVisible(visible);
    }

    public void initialize() {
        if(this.updateTask != null) {
            this.updateTask.cancel();
        }

        this.updateTask = Bukkit.getScheduler().runTaskTimer(this.plugin, this::update, 20, 2);
    }

    public void update() {
        if(this.scoreboard == null) return;

        this.objective.updateTitle(this.adapter.title(player));
        this.objective.updateLines(this.adapter.lines(player));
        this.objective.update();

        Component header = this.adapter.header(player), footer = this.adapter.footer(player);
        if(header != null && footer != null) this.setTab(header, footer);
    }
}
