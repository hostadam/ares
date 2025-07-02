package com.github.hostadam.ares.board;

import com.github.hostadam.ares.utils.PaperUtils;
import io.papermc.paper.scoreboard.numbers.NumberFormat;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.inventory.BlastingRecipe;
import org.bukkit.scoreboard.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Getter
public class BoardObjective {

    private static final String[] COLOR_CODES = Arrays
            .stream(ChatColor.values())
            .map(Object::toString)
            .toArray(String[]::new);
    private static final NumberFormat BLANK = NumberFormat.blank();

    private final Scoreboard scoreboard;
    private final Objective objective;
    private final String randomId;

    private Component title;
    private Set<String> previousLines;
    private final List<Component> lines = new ArrayList<>();
    private final Map<String, Team> teams = new HashMap<>();

    public BoardObjective(Scoreboard scoreboard) {
        this.scoreboard = scoreboard;
        this.title = Component.text("Loading...");
        this.previousLines = new HashSet<>();
        this.objective = scoreboard.registerNewObjective("buffered", Criteria.DUMMY, this.title);
        this.randomId = Integer.toHexString(ThreadLocalRandom.current().nextInt());
    }

    public boolean isVisible() {
        return this.objective.getDisplaySlot() == DisplaySlot.SIDEBAR;
    }

    public void setVisible(boolean visible) {
        if(visible) {
            this.objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        } else {
            this.scoreboard.clearSlot(DisplaySlot.SIDEBAR);
        }
    }

    private boolean updateLines(@NotNull List<Component> newLines) {
        final int oldLineSize = this.lines.size();
        final int newLineSize = newLines.size();

        // Early return if lines are empty.
        if(newLineSize == 0 && oldLineSize != 0) {
            this.lines.clear();
            return true;
        }

        boolean changed = newLineSize != oldLineSize ||
                IntStream.range(0, newLines.size())
                        .anyMatch(i -> !Objects.equals(this.lines.get(i), newLines.get(i)));

        if(!changed) return false;
        this.lines.clear();
        this.lines.addAll(newLines);
        return true;
    }

    public void applyChanges(Component title, List<Component> computedLines, BoardSettings settings) {
        if(!Objects.equals(this.title, title)) {
            this.title = title;
            this.objective.displayName(title);
        }

        boolean needsUpdate = this.updateLines(computedLines);
        if(needsUpdate) {
            Set<String> currentEntries = new HashSet<>();

            for(int index = 0; index < this.lines.size(); index++) {
                String entry = this.createEntryName(index);
                currentEntries.add(entry);

                String bukkitTeamName = "team_" + this.randomId + "_" + index;
                Team team = this.teams.computeIfAbsent(entry, k -> scoreboard.registerNewTeam(bukkitTeamName));
                if(!team.hasEntry(entry)) {
                    team.addEntry(entry);
                }

                team.prefix(this.lines.get(index));

                Score score = this.objective.getScore(entry);
                score.setScore(this.lines.size() - index);
                score.numberFormat(settings.getScoreFormat());
            }

            this.previousLines.removeAll(currentEntries);
            for (String oldEntry : this.previousLines) {
                this.scoreboard.resetScores(oldEntry);

                Team oldTeam = this.teams.remove(oldEntry);
                if (oldTeam != null) {
                    oldTeam.removeEntry(oldEntry);
                    oldTeam.unregister();
                }
            }

            this.previousLines = currentEntries;
        }
    }

    private String createEntryName(int index) {
        final int colorCodes = COLOR_CODES.length;
        if(index < colorCodes) return COLOR_CODES[index];

        int colorIndex = index % COLOR_CODES.length;
        int repeatCount = 1 + (index / COLOR_CODES.length);
        return String.join("", Collections.nCopies(repeatCount, COLOR_CODES[colorIndex]));
    }
}
