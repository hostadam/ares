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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

public record CommandNode(
        List<String> names,
        String usageMessage,
        String permission,
        String description,
        Predicate<CommandSender> requirement,
        Consumer<CommandContext> executor,
        List<Argument<?>> arguments,
        Map<String, CommandNode> children
) {

    public String primaryName() {
        return this.names.getFirst();
    }

    public List<String> secondaryNames() {
        return Collections.unmodifiableList(names.subList(1, names.size()));
    }

    public boolean testAccess(CommandSender sender) {
        return (this.requirement == null || this.requirement.test(sender)) && (this.permission == null || this.permission.isEmpty() || sender.hasPermission(this.permission));
    }

    public CommandNode getChild(String name) {
        return this.children.get(name.toLowerCase());
    }
}
