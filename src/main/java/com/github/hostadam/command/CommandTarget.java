package com.github.hostadam.command;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public enum CommandTarget {

    PLAYER {
        @Override
        public boolean testFor(CommandSender sender) {
            return sender instanceof Player;
        }
    },
    CONSOLE {
        @Override
        public boolean testFor(CommandSender sender) {
            return sender instanceof ConsoleCommandSender;
        }
    },
    ALL {
        @Override
        public boolean testFor(CommandSender sender) {
            return true;
        }
    };

    public abstract boolean testFor(CommandSender sender);
}
