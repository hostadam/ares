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

package com.github.hostadam.command;

import com.github.hostadam.command.argument.Argument;
import com.github.hostadam.command.dispatch.CommandContext;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class CommandNodeBuilder {

    private final String name;

    private List<String> aliases;
    private String usageMessage;
    private String description;
    private String permission;

    private Predicate<CommandSender> requirement = sender -> true;
    private Consumer<CommandContext> executor = commandContext -> {};

    private final List<Argument<?>> arguments;
    private final Map<String, CommandNode> children;

    public CommandNodeBuilder(String name) {
        this.name = name.toLowerCase();
        this.arguments = new ArrayList<>();
        this.children = new HashMap<>();
    }

    public CommandNodeBuilder aliases(String... names) {
        if(names == null || names.length == 0) {
            throw new IllegalArgumentException("Provided aliases may not be null or empty");
        }

        this.aliases = Arrays.stream(names).map(String::toLowerCase).toList();
        return this;
    }

    public CommandNodeBuilder usage(String usage) {
        this.usageMessage = usage;
        return this;
    }

    public CommandNodeBuilder description(String description) {
        this.description = description;
        return this;
    }

    public CommandNodeBuilder permission(String permission) {
        this.permission = permission;
        return this;
    }

    public CommandNodeBuilder requires(Predicate<CommandSender> requirement) {
        this.requirement = requirement;
        return this;
    }

    public <T> CommandNodeBuilder arg(@NotNull Argument<T> argument) {
        this.arguments.add(argument);
        return this;
    }

    public CommandNodeBuilder child(@NotNull CommandNode child) {
        child.names().forEach(string -> this.children.put(string.toLowerCase(), child));
        return this;
    }

    public CommandNodeBuilder executes(@NotNull Consumer<CommandContext> executor) {
        this.executor = executor;
        return this;
    }

    public CommandNode build() {
        List<String> names = this.aliases == null || this.aliases.isEmpty() ? List.of(this.name) : Stream.concat(Stream.of(this.name), this.aliases.stream()).toList();
        return new CommandNode(names, this.usageMessage, this.permission, this.description, this.requirement, this.executor, this.arguments, this.children);
    }
}
