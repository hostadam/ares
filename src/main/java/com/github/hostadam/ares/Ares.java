package com.github.hostadam.ares;

import com.github.hostadam.ares.board.BoardHandler;
import com.github.hostadam.ares.command.handler.CommandHandler;
import com.github.hostadam.ares.data.compat.AsyncBridge;

public interface Ares {

    AsyncBridge async();
    BoardHandler scoreboard();
    CommandHandler commands();
}
