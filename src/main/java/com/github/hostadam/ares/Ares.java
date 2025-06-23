package com.github.hostadam.ares;

import com.github.hostadam.ares.board.BoardHandler;
import com.github.hostadam.ares.command.handler.CommandHandler;

public interface Ares {

    BoardHandler scoreboard();
    CommandHandler commands();
}
