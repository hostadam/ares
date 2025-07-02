package com.github.hostadam.ares.board;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scoreboard.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
public class BoardObjective {

    private final Scoreboard scoreboard;
    private final Objective objective;

    private Component title;
    private Set<String> previousLines;
    private final Map<String, Team> teams = new HashMap<>();
    private final Map<Integer, Component> lines = new HashMap<>();

    private boolean shouldUpdateTitle = false, shouldUpdateLines = false;

    public BoardObjective(Scoreboard scoreboard) {
        this.scoreboard = scoreboard;
        this.title = Component.text("Loading...");
        this.previousLines = new HashSet<>();
        this.objective = scoreboard.registerNewObjective("buffered", Criteria.DUMMY, this.title);
    }

    public void setVisible(boolean visible) {
        if(visible) {
            this.objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        } else {
            this.scoreboard.clearSlot(DisplaySlot.SIDEBAR);
        }
    }

    public void updateTitle(Component newTitle) {
        Component newTitle = legacySerializer.deserialize(newTitleLegacy);
        if (!this.title.equals(newTitle)) {
            this.title = newTitle;
            this.shouldUpdateTitle = true;
        }
    }

    public void updateLine(int lineNumber, String line) {
        String currentLine = this.lines.get(lineNumber);
        if(!line.equals(currentLine)) {
            this.lines.put(lineNumber, line);
            this.shouldUpdateLines = true;
        }
    }

    private String[] getPrefixAndSuffix(String text) {
        String translated = ChatColor.translateAlternateColorCodes('&', text);
        if(translated.length() <= 64) {
            return new String[] { translated, "" };
        }

        int splitAt = text.charAt(63) == ChatColor.COLOR_CHAR ? 63 : 64;
        String prefix = translated.substring(0, splitAt);
        String suffix = ChatColor.getLastColors(prefix) + translated.substring(splitAt);
        suffix = suffix.length() > 64 ? suffix.substring(0, 64) : suffix;
        return new String[] { prefix, suffix };
    }

    public void updateLines(List<String> newLines) {
        if(newLines.size() != this.lines.size() || !this.lines.values().equals(newLines)) {
            this.lines.clear();

            if(newLines.isEmpty()) {
                this.shouldUpdateLines = true;
                return;
            }

            int totalLines = Math.min(64, newLines.size());
            for(int i = 0; i < totalLines; i++) {
                this.updateLine(totalLines - i, newLines.get(i));
            }
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

                Team team = this.teams.computeIfAbsent(name, scoreboard::registerNewTeam);
                team.setPrefix(text[0]);
                team.setSuffix(text[1]);

                if(!team.hasEntry(name)) team.addEntry(name);
                objective.getScore(name).setScore(index);

                newLines.add(name);
            });

            this.previousLines.forEach(previousLine -> {
                if(!newLines.contains(previousLine)) {
                    Team team = scoreboard.getTeam(previousLine);
                    if(team != null) team.removeEntry(previousLine);
                    scoreboard.resetScores(previousLine);
                }
            });

            this.previousLines = newLines;
            this.shouldUpdateLines = false;
        }
    }
}
