package com.github.hostadam.board;

import com.github.hostadam.board.nametag.NametagHandler;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

@Getter
public class Board {

    private transient JavaPlugin plugin;

    private Player player;
    private BoardObjective objective;
    @Setter
    private BoardAdapter adapter;

    private Scoreboard scoreboard;
    private BukkitRunnable updateTask;

    private boolean sidebarVisibility = true;
    @Setter
    private NametagHandler nametagHandler;

    public Board(JavaPlugin plugin, Player player, BoardAdapter adapter) {
        this.plugin = plugin;
        this.player = player;
        this.adapter = adapter;
        this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        this.objective = new BoardObjective(this.scoreboard);
        this.updateVisibility(true);

        player.setScoreboard(scoreboard);
    }

    public void setTab(String header, String footer) {
        this.player.setPlayerListHeaderFooter(header, footer);
    }

    public void remove() {
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
        }

        this.sidebarVisibility = visible;
        this.objective.setVisible(visible);
    }

    public void initialize() {
        if(this.updateTask != null) {
            this.updateTask.cancel();
        }

        (this.updateTask = new BukkitRunnable() {
            @Override
            public void run() {
                if(!sidebarVisibility) {
                    cancel();
                    return;
                }

                update();
            }
        }).runTaskTimerAsynchronously(this.plugin, 2, 2);
    }

    public void update() {
        if(this.scoreboard == null) return;

        this.objective.updateTitle(this.adapter.title(player));
        this.objective.updateLines(this.adapter.lines(player));
        this.objective.update();

        String[] tab = this.adapter.tab(player);
        if(tab != null && tab.length == 2) {
            this.setTab(tab[0], tab[1]);
        }
    }
}
