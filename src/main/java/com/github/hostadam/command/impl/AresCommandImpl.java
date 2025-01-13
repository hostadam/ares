package com.github.hostadam.command.impl;

import com.github.hostadam.command.AresCommand;
import com.github.hostadam.command.handler.CommandHandler;
import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;
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
        } else {
            if(args.length > 0) {
                String subcommandString = args[0];
                if(subcommandString.equalsIgnoreCase("help")) {
                    int page = 1;
                    if(args.length > 1 && Ints.tryParse(args[1]) != null) {
                        page = Integer.parseInt(args[1]);
                    }

                    this.sendUsage(sender, args, page - 1);
                    return true;
                }

                AresCommandData subcommand = this.getSubCommand(subcommandString);
                if(subcommand == null) {
                    this.sendUsage(sender, args, 0);
                    return true;
                }

                subcommand.execute(this.commandHandler, sender, Arrays.copyOfRange(args, 1, args.length));
            } else {
                this.sendUsage(sender, args, 0);
            }
        }

        return true;
    }

    private void sendUsage(CommandSender sender, String[] args, int page) {
        if(page < 0) {
            page = 0;
        }

        List<AresCommandData> subCommands;
        final int commandsPerPage = 10;
        final int maxPages = (int) Math.floor((double) this.subcommands.size() / (double) commandsPerPage);
        if(maxPages == 0) {
            subCommands = this.subcommands;
        } else if(page <= maxPages) {
            final int startOfRange = page * commandsPerPage;
            final int endOfRange = (page + 1) * commandsPerPage - 1;
            subCommands = this.subcommands.subList(startOfRange, Math.min(endOfRange, this.subcommands.size()));
        } else {
            sender.sendMessage("§cThere " + (maxPages != 1 ? "are" : "is") + " only " + maxPages + " page" + (maxPages != 1 ? "s" : "") + ".");
            return;
        }

        String mainLabel = data.getCommand().labels()[0];
        sender.sendMessage(" ");
        sender.sendMessage("§e§l" + mainLabel.toUpperCase() + " §7(Command Help)");
        sender.sendMessage("§7<> = §orequired§7, [] = §ooptional");
        sender.sendMessage(" ");
        for(AresCommandData subCommand : subCommands) {
            AresCommand command = subCommand.getCommand();
            sender.sendMessage("§e/" + mainLabel + " " + command.labels()[0] + " " + subCommand.getCommand().usage());
        }

        if(maxPages > 0) {
            sender.sendMessage(" ");
            sender.sendMessage("§7§oShowing page §f" + (page + 1) + " §7out of §f" + maxPages);
        }

        sender.sendMessage(" ");
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
