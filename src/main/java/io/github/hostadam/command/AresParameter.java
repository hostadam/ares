package io.github.hostadam.command;

import org.bukkit.command.CommandSender;

import java.util.List;

public interface AresParameter<T> {

    T parse(String argument);
    List<String> suggest(CommandSender sender, String textAlreadyEntered);
}
