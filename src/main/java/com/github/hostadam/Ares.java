package com.github.hostadam;

import com.github.hostadam.board.Board;
import com.github.hostadam.board.BoardAdapter;
import com.github.hostadam.board.BoardHandler;
import com.github.hostadam.command.ParameterConverter;
import com.github.hostadam.command.handler.CommandHandler;
import com.github.hostadam.command.impl.CommandImpl;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class Ares {

    private static Ares INSTANCE;

    private BoardHandler boardHandler;
    private CommandHandler commandHandler;

    public Ares(JavaPlugin parent) {
        this.boardHandler = new BoardHandler(parent);
        this.commandHandler = new CommandHandler();
    }

    public void setScoreboardAdapter(BoardAdapter adapter) {
        this.boardHandler.setAdapter(adapter);
        //TODO: Refresh for all online players.
    }

    /** Board **/
    public Board getScoreboard(Player player) {
        return this.boardHandler.getScoreboard(player.getUniqueId());
    }

    public Board getScoreboard(UUID uniqueId) {
        return this.boardHandler.getScoreboard(uniqueId);
    }

    /** Commands **/
    public void registerCommand(Object object) {
        this.commandHandler.register(object);
    }

    public CommandImpl getCommand(String name) {
        return this.commandHandler.getCommandByLabel(name);
    }

    public void addParameterConverter(Class<?> clazz, ParameterConverter<?> converter) {
        this.commandHandler.addConverter(clazz, converter);
    }

    public <T> ParameterConverter<T> getParameterConverter(Class<T> clazz) {
        return this.commandHandler.getConverter(clazz);
    }

    public static boolean init(JavaPlugin plugin) {
        if(INSTANCE != null) {
            return false;
        }

        INSTANCE = new Ares(plugin);
        return true;
    }

    public static Ares get() {
        return INSTANCE;
    }
}
