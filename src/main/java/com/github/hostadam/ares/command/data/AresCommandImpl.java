package com.github.hostadam.ares.command.data;

import com.github.hostadam.ares.command.tabcompletion.ParameterTabCompleter;
import com.github.hostadam.ares.command.handler.CommandHandler;
import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class AresCommandImpl extends Command {

    private final CommandHandler commandHandler;
    private final AresCommandData command;
    private final Map<String, AresCommandData> subcommands = new ConcurrentHashMap<>();

    public AresCommandImpl(CommandHandler handler, AresCommandData command) {
        super(command.getMainLabel(), command.getDescription(), command.getUsageMessage(), command.getAliases());
        this.setPermission(command.getPermission());

        this.commandHandler = handler;
        this.command = command;
    }

    public void addSubCommand(AresCommandData command) {
        for(String label : command.getCommandLabels()) {
            this.subcommands.put(label.toLowerCase(), command);
        }
    }

    public AresCommandData getSubCommand(String name) {
        return this.subcommands.get(name.toLowerCase());
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if(!this.command.getPermission().isEmpty() && !sender.hasPermission(this.command.getPermission())) {
            sender.sendMessage("§cNo permission.");
            return true;
        }

        if(args.length < this.command.getRequiredArgs() || (args.length == 0 && !this.subcommands.isEmpty())) {
            this.command.execute(this.commandHandler.context(), sender, args);
            return true;
        }

        if(this.subcommands.isEmpty()) {
            this.command.execute(this.commandHandler.context(), sender, args);
        } else {
            String arg = args[0].toLowerCase();
            if(arg.equals("help")) {
                int page = 1;
                if (args.length > 1) {
                    Integer parsed = Ints.tryParse(args[1]);
                    if (parsed != null) page = parsed;
                }

                sendUsage(sender, page - 1);
                return true;
            }

            AresCommandData subcommand = this.getSubCommand(arg);
            if(subcommand == null) {
                this.sendUsage(sender, 0);
                return true;
            }

            if(!subcommand.getPermission().isEmpty() && !sender.hasPermission(subcommand.getPermission())) {
                sender.sendMessage("§cNo permission.");
                return true;
            }

            String[] subCommandArgs = Arrays.copyOfRange(args, 1, args.length);
            if(subCommandArgs.length <= subcommand.getRequiredArgs()) {
                sender.sendMessage("§cUsage: /" + label + " " + arg + " " + subcommand.getUsageMessage());
                return true;
            }

            subcommand.execute(this.commandHandler.context(), sender, subCommandArgs);
        }

        return true;
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, String[] args, @Nullable Location location) {
        int argCount = args.length;
        if(argCount > 0) {
            int argPos = argCount - 1;
            Class<?> clazz = this.command.getTabCompleterClass(argPos);
            if(clazz != null) {
                ParameterTabCompleter<?> tabCompleter = this.commandHandler.context().getTabCompletion(clazz);
                if(tabCompleter == null) return Collections.emptyList();
                return tabCompleter.suggest(sender, args[argPos]);
            }
        }

        return super.tabComplete(sender, alias, args, location);
    }

    private void sendUsage(CommandSender sender, int page) {
        if(page < 0) page = 0;

        List<AresCommandData> allSubCommands = this.subcommands.values()
                .stream()
                .filter(command -> command.getPermission().isEmpty() || sender.hasPermission(command.getPermission()))
                .toList();

        List<AresCommandData> subCommands;
        final int commandsPerPage = 10;
        final int maxPages = (int) Math.floor((double) allSubCommands.size() / (double) commandsPerPage);

        if(maxPages == 0) {
            subCommands = Lists.newArrayList(allSubCommands);
        } else if(page <= maxPages) {
            final int startOfRange = page * commandsPerPage;
            final int endOfRange = (page + 1) * commandsPerPage - 1;
            subCommands = allSubCommands.subList(startOfRange, Math.min(endOfRange, allSubCommands.size()));
        } else {
            sender.sendMessage("§cThere are only " + (maxPages + 1) + " pages.");
            return;
        }

        if(subCommands.isEmpty()) {
            sender.sendMessage("§cNo permission.");
            return;
        }

        String mainLabel = this.command.getMainLabel();
        sender.sendMessage(" ");
        sender.sendMessage("§e§l" + mainLabel.toUpperCase() + " §7(Command Help)");
        sender.sendMessage("§7<> = §orequired§7, [] = §ooptional");
        sender.sendMessage(" ");
        for(AresCommandData subCommand : subCommands) {
            sender.sendMessage("§e/" + mainLabel + " " + subCommand.getMainLabel() + " " + subCommand.getUsageMessage());
        }

        if(maxPages > 0) {
            sender.sendMessage(" ");
            sender.sendMessage("§7§oShowing page §f" + (page + 1) + " §7§oout of §f" + (maxPages + 1));
        }

        sender.sendMessage(" ");
    }
}
