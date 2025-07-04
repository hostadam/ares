package com.github.hostadam.ares.command.tabcompletion;

import org.bukkit.command.CommandSender;

import java.util.List;

@FunctionalInterface
public interface ParameterTabCompleter {

    List<String> suggest(CommandSender sender, String input);
}
