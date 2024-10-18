package com.github.hostadam.board.nametag;

import com.github.hostadam.board.Board;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class NametagHandler {

    private static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyz";
    private final Scoreboard board;

    public NametagHandler(Board board) {
        this.board = board.getScoreboard();
    }

    public void replace(String oldTeamName, String newTeamName, String entry) {
        Team team = this.getTeam(oldTeamName);
        if(team != null) {
            team.removeEntry(entry);
        }

        Team newTeam = this.getTeam(newTeamName);
        if(newTeam != null) {
            newTeam.addEntry(entry);
        }
    }

    private String peel(String name) {
        return name.split("-")[1];
    }

    public Team getTeam(String name) {
       if(this.board == null) return null;
       String peel = (name.contains("-") ? this.peel(name) : name);
       return this.board.getTeam(peel);
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
}
