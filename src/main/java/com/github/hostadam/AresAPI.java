package com.github.hostadam;

import com.github.hostadam.board.BoardAdapter;

import java.util.logging.Level;
import java.util.logging.Logger;

public class AresAPI {

    private static final Logger LOGGER = Logger.getLogger("ares");

    public static void log(String log) {
        log(Level.INFO, log);
    }

    public static void log(Level level, String log) {
        LOGGER.log(level, log);
    }
}
