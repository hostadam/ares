package com.github.hostadam.ares.board.nametag;

import com.github.hostadam.ares.board.Board;
import org.bukkit.Bukkit;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class NametagHandler {

    private static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyz";
    private final Scoreboard board;

    public NametagHandler(Board board) {
        this.board = board.getScoreboard();
    }

    public void switchTeamOfPlayer(String oldTeamName, String newTeamName, String playerName) {
        Team team = this.getTeam(oldTeamName);
        if(team != null) team.removeEntry(playerName);

        Team newTeam = this.getTeam(newTeamName);
        if(newTeam != null) newTeam.addEntry(playerName);
    }

    public Team getTeam(String teamName) {
       if(this.board == null) return null;
       String cleanedTeamName = this.cleanTeamName(teamName);
       return this.board.getTeam(cleanedTeamName);
    }

    public Team createTeam(String teamName, int priority) {
        Team team = this.getTeam(teamName);
        if(team != null) {
            return team;
        }

        int iteration = (int) Math.ceil((double) priority / CHARACTERS.length());
        int remainder = priority % CHARACTERS.length();

        String teamNameIdentifier = String.valueOf(CHARACTERS.charAt(remainder)).repeat(iteration) + "-" + teamName;
        return this.board.registerNewTeam(teamNameIdentifier);
    }

    private String cleanTeamName(String teamName) {
        return teamName.contains("-") ? teamName.split("-")[1] : teamName;
    }

    public void shutdown() {}
}
