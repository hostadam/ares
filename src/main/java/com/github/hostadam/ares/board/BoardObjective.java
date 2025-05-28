package com.github.hostadam.ares.board;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scoreboard.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
public class BoardObjective {

    private Scoreboard scoreboard;
    private Objective objective;

    private String title;
    private Set<String> previousLines;
    private Map<Integer, String> lines;
    private final Map<String, Team> teams = new HashMap<>();

    private boolean shouldUpdateTitle = false, shouldUpdateLines = false;

    public BoardObjective(Scoreboard scoreboard) {
        this.scoreboard = scoreboard;
        this.title = "Loading...";
        this.previousLines = new HashSet<>();
        this.lines = new ConcurrentHashMap<>();
        this.objective = scoreboard.registerNewObjective("buffered", Criteria.DUMMY, this.title);
    }

    public void setVisible(boolean visible) {
        if(visible) {
            this.objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        } else {
            this.scoreboard.clearSlot(DisplaySlot.SIDEBAR);
        }
    }

    public void updateTitle(String title) {
        if(!this.title.equals(title)) {
            this.title = title;
            this.shouldUpdateTitle = true;
        }
    }

    public void updateLine(int lineNumber, String line) {
        String currentLine = this.lines.get(lineNumber);
        if(currentLine == null || !currentLine.equals(line)) {
            this.lines.put(lineNumber, line);
            this.shouldUpdateLines = true;
        }
    }

    private String[] getPrefixAndSuffix(String text) {
        String prefix, suffix;
        String newText = ChatColor.translateAlternateColorCodes('&', text);
        if(newText.length() < 64) {
            prefix = text;
            suffix = "";
        } else {
            int splitAt = text.charAt(63) == ChatColor.COLOR_CHAR ? 63 : 64;
            prefix = text.substring(0, splitAt);
            suffix = text.substring(0, Math.min((ChatColor.getLastColors(prefix) + text.substring(splitAt)).length(), 64));
        }

        return new String[] { prefix, suffix };
    }

    public void updateLines(List<String> lines) {
        if(this.lines.size() != lines.size() || !this.lines.equals(lines)) {
            this.lines.clear();

            if(lines.isEmpty()) {
                this.shouldUpdateLines = true;
                return;
            }
        }

        int count = 0;
        int totalLines = Math.min(64, lines.size());
        for(String line : lines) {
            this.updateLine(totalLines - count++, line);
        }
    }

    public void update() {
        if(shouldUpdateTitle) {
            this.objective.setDisplayName(this.title);
            this.shouldUpdateTitle = false;
        }

        if(shouldUpdateLines) {
            Set<String> newLines = new HashSet<>();

            this.lines.forEach((index, line) -> {
                String name = "§" + ChatColor.values()[index].getChar();
                String[] text = this.getPrefixAndSuffix(line);

                Team team = this.teams.computeIfAbsent(name, t -> scoreboard.registerNewTeam(t));
                team.setPrefix(text[0]);
                team.setSuffix(text[1]);

                newLines.add(name);

                if(!team.hasEntry(name)) team.addEntry(name);
                objective.getScore(name).setScore(index);
            });

            this.previousLines.removeAll(newLines);
            this.previousLines.removeIf(string -> {
                Team team = this.scoreboard.getTeam(string);
                if(team != null) {
                    team.removeEntry(string);
                }

                this.scoreboard.resetScores(string);
                return true;
            });

            this.previousLines = newLines;
            this.shouldUpdateLines = false;
        }
    }
}
