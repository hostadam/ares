package com.github.hostadam;

import com.github.hostadam.board.Board;
import com.github.hostadam.board.BoardAdapter;
import com.github.hostadam.board.BoardHandler;
import com.github.hostadam.command.parameter.ParameterConverter;
import com.github.hostadam.command.handler.CommandHandler;
import com.github.hostadam.command.impl.AresCommandImpl;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class Ares implements AresInterface {

    private static Ares INSTANCE;

    private BoardHandler boardHandler;
    private CommandHandler commandHandler;

    public Ares(JavaPlugin parent) {
        this.boardHandler = new BoardHandler(parent);
        this.commandHandler = new CommandHandler();
    }

    @Override
    public void setScoreboardAdapter(BoardAdapter adapter) {
        this.boardHandler.setAdapter(adapter);
        this.boardHandler.refresh();
    }

    @Override
    public Board getScoreboard(Player player) {
        return this.boardHandler.getScoreboard(player.getUniqueId());
    }

    @Override
    public Board getScoreboard(UUID uniqueId) {
        return this.boardHandler.getScoreboard(uniqueId);
    }

    @Override
    public void registerCommand(Object object) {
        this.commandHandler.register(object);
    }

    @Override
    public AresCommandImpl getCommand(String name) {
        return this.commandHandler.getCommandByLabel(name);
    }

    @Override
    public void addParameterConverter(Class<?> clazz, ParameterConverter<?> converter) {
        this.commandHandler.addConverter(clazz, converter);
    }

    @Override
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
