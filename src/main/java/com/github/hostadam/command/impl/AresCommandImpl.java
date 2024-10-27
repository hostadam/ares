package com.github.hostadam.command.impl;

import com.github.hostadam.command.handler.CommandHandler;
import com.google.common.collect.Lists;
import lombok.Getter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
public class AresCommandImpl extends Command {

    private final CommandHandler commandHandler;

    private AresCommandData data;
    private List<AresCommandData> subcommands = new ArrayList<>();

    public AresCommandImpl(CommandHandler handler, AresCommandData data) {
        super(data.getCommand().labels()[0], data.getCommand().description(), data.getCommand().usage(), data.getCommand().labels().length > 1 ? List.of(Arrays.copyOfRange(data.getCommand().labels(), 1, data.getCommand().labels().length)) : Lists.newArrayList());
        this.setPermission(data.getCommand().permission());
        this.data = data;
        this.commandHandler = handler;
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if(this.subcommands.isEmpty()) {
            this.data.execute(this.commandHandler, sender, args);
        } else if(args.length > 0) {
            AresCommandData subcommand = this.getSubCommand(args[0]);
            subcommand.execute(this.commandHandler, sender, Arrays.copyOfRange(args, 0, args.length));
        } else {
            //TODO: Send fancy usage.
        }

        return true;
    }

    public void addSubCommand(AresCommandData data) {
        this.subcommands.add(data);
    }

    public AresCommandData getSubCommand(String name) {
        for(AresCommandData command : this.subcommands) {
            for(String string : command.getCommand().labels()) {
                if(string.equalsIgnoreCase(name)) {
                    return command;
                }
            }
        }

        return null;
    }
}
