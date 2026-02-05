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

package com.github.hostadam.command.wrapper;

import com.github.hostadam.command.CommandNode;
import com.github.hostadam.command.argument.Argument;
import com.github.hostadam.command.dispatch.CommandDispatcher;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BukkitCommandNode extends Command {

    private final CommandDispatcher dispatcher;
    private final CommandNode command;

    public BukkitCommandNode(CommandDispatcher dispatcher, CommandNode node) {
        super(node.primaryName(), node.description(), node.usageMessage(), node.secondaryNames());
        this.setPermission(node.permission());

        this.command = node;
        this.dispatcher = dispatcher;
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        return this.dispatcher.dispatch(sender, command, args);
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, String[] args, @Nullable Location location) {
        final Pair<CommandNode, Integer> nodeIntegerPair = this.dispatcher.findNode(this.command, args);
        final int argIndex = nodeIntegerPair.getRight();

        CommandNode node = nodeIntegerPair.getLeft();
        if(!node.testAccess(sender)) {
            return List.of();
        }

        Argument<?> argument = argIndex < node.arguments().size() ? node.arguments().get(argIndex) : null;
        if(argument != null) {
            String currentInput = args[args.length - 1];
            return argument.type().suggests(sender, currentInput);
        }

        if (!node.children().isEmpty() && argIndex < 1) {
            String partial = args[0].toLowerCase();
            return node.children().keySet().stream()
                    .filter(name -> name.toLowerCase().startsWith(partial))
                    .toList();
        }

        return List.of();
    }
}
