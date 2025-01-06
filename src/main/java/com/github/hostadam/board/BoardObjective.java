package com.github.hostadam.board;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scoreboard.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
public class BoardObjective {

    private Scoreboard scoreboard;
    private Objective objective;

    private String title;
    private Map<Integer, String> lines;
    private final Map<String, Team> teams = new HashMap<>();
    private boolean shouldUpdateTitle = false, shouldUpdateLines = false;

    public BoardObjective(Scoreboard scoreboard) {
        this.scoreboard = scoreboard;
        this.title = "Loading...";
        this.lines = new ConcurrentHashMap<>();
        this.objective = scoreboard.registerNewObjective("buffered", Criteria.DUMMY, this.title);
    }

    public void updateTitle(String title) {
        this.title = title;
        this.shouldUpdateTitle = true;
    }

    public void setVisible(boolean visible) {
        if(visible) {
            this.objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        } else {
            this.scoreboard.clearSlot(DisplaySlot.SIDEBAR);
        }
    }

    public void updateLines(List<String> lines) {
        if(!this.lines.equals(lines)) {
            this.lines.clear();

            if(lines.isEmpty()) {
                this.shouldUpdateLines = true;
                return;
            }
        }

        int lineLength = Math.min(16, lines.size());
        for(int i = 0; i < lineLength; i++) {
            this.updateLine(lineLength - i, lines.get(i));
        }
    }

    public void updateLine(int lineNumber, String line) {
        String currentLine = this.lines.get(lineNumber);
        if(currentLine == null || !currentLine.equals(line)) {
            this.lines.put(lineNumber, line);
            this.shouldUpdateLines = true;
        }
    }

    public void update() {
        if(shouldUpdateTitle) {
            this.objective.setDisplayName(this.title);
            this.shouldUpdateTitle = false;
        }

        this.lines.forEach((index, line) -> {
            String name = "§" + ChatColor.values()[index].getChar();

            Team team = this.teams.computeIfAbsent(name, t -> scoreboard.registerNewTeam(t));
            team.setPrefix(line);

            if(!team.hasEntry(name)) team.addEntry(name);
            objective.getScore(name).setScore(index);
        });

        this.shouldUpdateLines = false;
    }
}
