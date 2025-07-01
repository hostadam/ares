package com.github.hostadam.ares.command.context;

import org.bukkit.command.CommandSender;

import java.util.List;

@FunctionalInterface
public interface ParameterTabCompleter<T> {

    List<String> suggest(CommandSender sender, String input);
}
