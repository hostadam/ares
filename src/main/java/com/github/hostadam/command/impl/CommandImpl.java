package com.github.hostadam.command.impl;

import com.github.hostadam.command.handler.CommandHandler;
import com.google.common.collect.Lists;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
public class CommandImpl extends Command {

    private final CommandHandler commandHandler;
    private CommandData data;
    private List<CommandData> subcommands = new ArrayList<>();

    public CommandImpl(CommandHandler handler, CommandData data) {
        super(data.getCommand().labels()[0], data.getCommand().description(), data.getCommand().usage(), data.getCommand().labels().length > 1 ? List.of(Arrays.copyOfRange(data.getCommand().labels(), 1, data.getCommand().labels().length)) : Lists.newArrayList());
        this.setPermission(data.getCommand().permission());
        this.data = data;
        this.commandHandler = handler;
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        CommandData subcommand;
        if(args.length > 0 && (subcommand = this.getSubCommand(args[0])) != null) {
            subcommand.execute(this.commandHandler, sender, Arrays.copyOfRange(args, 1, args.length));
        } else {
            this.data.execute(this.commandHandler, sender, args);
        }

        return true;
    }

    public void addSubCommand(CommandData data) {
        this.subcommands.add(data);
    }

    public CommandData getSubCommand(String name) {
        for(CommandData command : this.subcommands) {
            Bukkit.broadcastMessage("main = " + this.getName() + ", sub = " + Arrays.toString(command.getCommand().labels()));
            for(String label : command.getCommand().labels()) {
                if(label.equalsIgnoreCase(name)) {
                    return command;
                }
            }
        }

        return null;
    }
}
