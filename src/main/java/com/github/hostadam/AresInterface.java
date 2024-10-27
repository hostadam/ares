package com.github.hostadam;

import com.github.hostadam.board.Board;
import com.github.hostadam.board.BoardAdapter;
import com.github.hostadam.command.impl.AresCommandImpl;
import com.github.hostadam.command.parameter.ParameterConverter;
import org.bukkit.entity.Player;

import java.util.UUID;

public interface AresInterface {

    void setScoreboardAdapter(BoardAdapter adapter);
    Board getScoreboard(Player player);
    Board getScoreboard(UUID uniqueId);

    void registerCommand(Object object);
    AresCommandImpl getCommand(String name);

    void addParameterConverter(Class<?> clazz, ParameterConverter<?> converter);
    <T> ParameterConverter<T> getParameterConverter(Class<T> clazz);
}
