package io.github.hostadam.command.impl;

import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class CommandImpl extends Command {

    private final CommandRegistry commandRegistry;
    private final CommandData command;
    private final Map<String, CommandData> subcommands = new ConcurrentHashMap<>();

    public CommandImpl(CommandRegistry handler, CommandData command) {
        super(command.getMainLabel(), command.getDescription(), command.getUsageMessage(), command.getAliases());
        this.setPermission(command.getPermission());

        this.commandRegistry = handler;
        this.command = command;
    }

    public void addSubCommand(CommandData command) {
        this.subcommands.put(command.getCommandLabels()[0].toLowerCase(), command);
    }

    public CommandData getSubCommand(String name) {
        CommandData data = this.subcommands.get(name.toLowerCase());
        return data != null ? data : this.subcommands.values().stream().filter(commandData -> commandData.getAliases().contains(name)).findAny().orElse(null);
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if(!this.command.getPermission().isEmpty() && !sender.hasPermission(this.command.getPermission())) {
            sender.sendMessage(Component.text("No permission.", NamedTextColor.RED));
            return true;
        }

        if(this.command.getRequiredArgs() > 0 && args.length < this.command.getRequiredArgs()) {
            sender.sendMessage(Component.text("Usage: /" + label + " " + command.getUsageMessage(), NamedTextColor.RED));
            return true;
        }

        if(this.subcommands.isEmpty()) {
            this.command.execute(this.commandRegistry.context(), sender, args);
        } else if(args.length == 0) {
            if(this.command.isSendUsagePrioritized()) {
                this.sendUsage(sender, 1);
            } else {
                this.command.execute(this.commandRegistry.context(), sender, args);
            }
        } else {
            String arg = args[0].toLowerCase();
            if(arg.equals("help")) {
                int page = 1;
                if (args.length > 1) {
                    Integer parsed = Ints.tryParse(args[1]);
                    if (parsed != null) page = parsed;
                }

                sendUsage(sender, page);
                return true;
            }

            CommandData subcommand = this.getSubCommand(arg);
            if(subcommand == null) {
                this.sendUsage(sender, 1);
                return true;
            }

            if(!subcommand.getPermission().isEmpty() && !sender.hasPermission(subcommand.getPermission())) {
                sender.sendMessage(Component.text("No permission.", NamedTextColor.RED));
                return true;
            }

            String[] subCommandArgs = Arrays.copyOfRange(args, 1, args.length);
            if(subCommandArgs.length < subcommand.getRequiredArgs()) {
                sender.sendMessage(Component.text("Usage: /" + label + " " + arg + " " + subcommand.getUsageMessage(), NamedTextColor.RED));
                return true;
            }

            subcommand.execute(this.commandRegistry.context(), sender, subCommandArgs);
        }

        return true;
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, String[] args, @Nullable Location location) {
        int argCount = args.length;

        if(!this.subcommands.isEmpty()) {
            if(argCount == 0) {
                return new ArrayList<>(this.subcommands.keySet());
            }

            String first = args[0].toLowerCase();
            if(argCount == 1) {
                return this.subcommands.keySet().stream().filter(label -> label.startsWith(first)).toList();
            }

            CommandData sub = this.getSubCommand(first);
            if(sub != null) {
                int subArgPos = argCount - 2;
                Class<?> clazz = sub.getTabCompleterClass(subArgPos);
                if(clazz != null) {
                    ParameterTabCompleter tabCompleter = this.commandRegistry.context().getTabCompletion(clazz);
                    if(tabCompleter != null) {
                        return tabCompleter.suggest(sender, args[argCount - 1].toLowerCase());
                    }
                }
            }

            return Collections.emptyList();
        }

        int argPos = Math.max(0, argCount - 1);
        Class<?> clazz = this.command.getTabCompleterClass(argPos);
        if(clazz != null) {
            ParameterTabCompleter tabCompleter = this.commandRegistry.context().getTabCompletion(clazz);
            if(tabCompleter != null) {
                return tabCompleter.suggest(sender, args[argPos].toLowerCase());
            }
        }

        return super.tabComplete(sender, alias, args, location);
    }

    private void sendUsage(CommandSender sender, int page) {
        int adjustedPage = page - 1;

        List<CommandData> allSubCommands = this.subcommands.values()
                .stream()
                .filter(command -> command.getPermission().isEmpty() || sender.hasPermission(command.getPermission()))
                .toList();

        List<CommandData> subCommands;
        final int commandsPerPage = 10;
        final int maxPages = (int) Math.ceil((double) allSubCommands.size() / (double) commandsPerPage);

        if(maxPages <= 1) {
            subCommands = Lists.newArrayList(allSubCommands);
        } else if(page <= maxPages) {
            final int startOfRange = adjustedPage * commandsPerPage;
            final int endOfRange = (page) * commandsPerPage - 1;
            subCommands = allSubCommands.subList(startOfRange, Math.min(endOfRange, allSubCommands.size()));
        } else {
            sender.sendMessage(Component.text("There are only " + maxPages + " pages.", NamedTextColor.RED));
            return;
        }

        if(subCommands.isEmpty()) {
            sender.sendMessage(Component.text("No commands available.", NamedTextColor.RED));
            return;
        }

        String mainLabel = this.command.getMainLabel();
        sender.sendMessage(Component.space());
        sender.sendMessage(Component.text(mainLabel.toUpperCase(), NamedTextColor.YELLOW, TextDecoration.BOLD).append(Component.text(" (Command Help)", NamedTextColor.GRAY).decoration(TextDecoration.BOLD, false)));
        sender.sendMessage(Component.text("<> = required, [] = optional", NamedTextColor.GRAY));
        sender.sendMessage(Component.space());

        for(CommandData subCommand : subCommands) {
            sender.sendMessage(Component.text("/" + mainLabel + " " + subCommand.getMainLabel() + " " + subCommand.getUsageMessage(), NamedTextColor.YELLOW).append(Component.text(" - " + subCommand.getDescription(), NamedTextColor.GRAY)));
        }

        sender.sendMessage(Component.space());

        boolean hasPreviousPage = page > 1;
        boolean hasNextPage = page < maxPages;
        if(hasPreviousPage || hasNextPage) {
            List<Component> paginationParts = new ArrayList<>();

            if(hasPreviousPage) {
                Component previousPage = Component.text("← Previous Page")
                        .color(NamedTextColor.RED)
                        .clickEvent(ClickEvent.runCommand(this.getLabel() + " help " + adjustedPage))
                        .hoverEvent(HoverEvent.showText(Component.text("Go to page " + adjustedPage, NamedTextColor.GREEN)));
                paginationParts.add(previousPage);
            }

            if(hasPreviousPage && hasNextPage) {
                paginationParts.add(Component.text(" | ").color(NamedTextColor.GRAY));
            }

            if(hasNextPage) {
                Component nextPage = Component.text("Next Page →")
                        .color(NamedTextColor.GREEN)
                        .clickEvent(ClickEvent.runCommand(this.getLabel() + " help " + (page + 1)))
                        .hoverEvent(HoverEvent.showText(Component.text("Go to page " + (page + 1), NamedTextColor.GREEN)));
                paginationParts.add(nextPage);
            }

            Component finalPaginationComponent = Component.join(JoinConfiguration.separator(Component.empty()), paginationParts);
            sender.sendMessage(finalPaginationComponent);
        }
    }
}
