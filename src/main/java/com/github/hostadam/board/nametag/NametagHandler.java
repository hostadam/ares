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

    private String peel(String name) {
        return name.split("-")[1];
    }

    public Team getTeam(String name) {
       if(this.board == null) return null;
       return this.board.getTeam(this.peel(name));
    }

    public Team createTeam(String teamName, int priority) {
        String peeled = this.peel(teamName);
        Team team = this.getTeam(peeled);
        if(team != null) {
            return team;
        }

        int iteration = (int) Math.ceil((double) priority / CHARACTERS.length());
        int remainder = priority % CHARACTERS.length();

        String teamNameIdentifier = String.valueOf(CHARACTERS.charAt(remainder)).repeat(iteration) + "-" + teamName;
        return this.board.registerNewTeam(teamNameIdentifier);
    }

}
