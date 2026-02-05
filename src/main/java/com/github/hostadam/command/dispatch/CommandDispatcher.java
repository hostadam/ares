/*
 * MIT License
 * Copyright (c) 2026 Hostadam
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.hostadam.command.dispatch;

import com.google.common.collect.Lists;
import com.github.hostadam.command.CommandNode;
import com.github.hostadam.command.argument.Argument;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.command.CommandSender;

import java.util.*;

public class CommandDispatcher {

    private static final Component NO_ACCESS = Component.text("You may not execute this command.", NamedTextColor.RED);

    public Pair<CommandNode, Integer> findNode(CommandNode rootNode, String[] providedArgs) {
        if(providedArgs == null) {
            return Pair.of(rootNode, 0);
        }

        final int argLength = providedArgs.length;
        int argumentStartIndex = 0;

        CommandNode node = rootNode;
        if(argLength > 0) {
            while(argumentStartIndex < argLength) {
                String arg = providedArgs[argumentStartIndex];
                CommandNode child = node.getChild(arg);
                if(child == null) break;
                node = child;
                argumentStartIndex++;
            }
        }

        return Pair.of(node, argumentStartIndex);
    }

    public boolean dispatch(CommandSender sender, CommandNode rootNode, String[] providedArgs) {
        final int argLength = providedArgs.length;
        final Pair<CommandNode, Integer> nodeIndexPair = this.findNode(rootNode, providedArgs);

        CommandNode node = nodeIndexPair.getLeft();
        int argumentStartIndex = nodeIndexPair.getRight();

        if(!node.testAccess(sender)) {
            sender.sendMessage(NO_ACCESS);
            return false;
        }

        String[] normalizedArgs = Arrays.copyOfRange(providedArgs, argumentStartIndex, argLength);
        Map<String, Object> args = this.parseArguments(sender, node, normalizedArgs);
        if(args == null) return false;

        CommandContext context = new CommandContext(sender, args);
        node.executor().accept(context);
        return true;
    }

    private void sendUsageMessage(CommandSender sender, CommandNode node, int page) {
        if(node.children().isEmpty()) {
            sender.sendMessage(node.usageMessage());
            return;
        }

        int adjustedPage = page - 1;
        List<CommandNode> allChildren = node.children().values()
                .stream()
                .filter(command -> command.testAccess(sender))
                .toList();

        List<CommandNode> children;
        final int commandsPerPage = 10;
        final int maxPages = (int) Math.ceil((double) allChildren.size() / (double) commandsPerPage);

        if (maxPages <= 1) {
            children = Lists.newArrayList(allChildren);
        } else if (page <= maxPages) {
            final int startOfRange = adjustedPage * commandsPerPage;
            final int endOfRange = (page) * commandsPerPage - 1;
            children = allChildren.subList(startOfRange, Math.min(endOfRange, allChildren.size()));
        } else {
            sender.sendMessage(Component.text("There are only " + maxPages + " pages.", NamedTextColor.RED));
            return;
        }

        if (children.isEmpty()) {
            sender.sendMessage(Component.text("No commands available.", NamedTextColor.RED));
            return;
        }

        sender.sendMessage(Component.space());
        sender.sendMessage(Component.text("Command Help", NamedTextColor.YELLOW).decoration(TextDecoration.BOLD, false));
        sender.sendMessage(Component.text("<> = required, [] = optional", NamedTextColor.GRAY));
        sender.sendMessage(Component.space());

        for (CommandNode commandNode : children) {
            sender.sendMessage(Component.text(commandNode.usageMessage(), NamedTextColor.YELLOW).append(Component.text(" - " + commandNode.description(), NamedTextColor.GRAY)));
        }

        sender.sendMessage(Component.space());

        boolean hasPreviousPage = page > 1;
        boolean hasNextPage = page < maxPages;
        if (hasPreviousPage || hasNextPage) {
            List<Component> paginationParts = new ArrayList<>();

            if (hasPreviousPage) {
                Component previousPage = Component.text("← Previous Page")
                        .color(NamedTextColor.RED)
                        .clickEvent(ClickEvent.callback(audience -> {
                            if(audience instanceof CommandSender commandSender) {
                                this.sendUsageMessage(commandSender, node, adjustedPage);
                            }
                        }))
                        .hoverEvent(HoverEvent.showText(Component.text("Go to page " + adjustedPage, NamedTextColor.GREEN)));
                paginationParts.add(previousPage);
            }

            if (hasPreviousPage && hasNextPage) {
                paginationParts.add(Component.text(" | ").color(NamedTextColor.GRAY));
            }

            if (hasNextPage) {
                Component nextPage = Component.text("Next Page →")
                        .color(NamedTextColor.GREEN)
                        .clickEvent(ClickEvent.callback(audience -> {
                            if(audience instanceof CommandSender commandSender) {
                                this.sendUsageMessage(commandSender, node, page + 1);
                            }
                        }))
                        .hoverEvent(HoverEvent.showText(Component.text("Go to page " + (page + 1), NamedTextColor.GREEN)));
                paginationParts.add(nextPage);
            }

            Component finalPaginationComponent = Component.join(JoinConfiguration.separator(Component.empty()), paginationParts);
            sender.sendMessage(finalPaginationComponent);
        }
    }

    private Map<String, Object> parseArguments(CommandSender sender, CommandNode node, String[] args) {
        Map<String, Object> parsedArgs = new HashMap<>();
        List<Argument<?>> arguments = node.arguments();

        for(int index = 0; index < arguments.size(); index++) {
            Argument<?> argument = arguments.get(index);
            String provided = index < args.length ? args[index] : null;

            if(provided == null) {
                // If optional argument and not provided, we just continue.
                if(argument.optional()) continue;
                this.sendUsageMessage(sender, node, 1);
                return null;
            }

            Optional<?> parsed = argument.type().parseArg(provided);
            if(parsed.isEmpty()) {
                sender.sendMessage(argument.errorMessage().apply(provided));
                return null;
            }

            parsedArgs.put(argument.key(), parsed.get());
        }

        return parsedArgs;
    }
}
