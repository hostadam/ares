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

package com.github.hostadam.utilities;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class InputHandler {

    private final Map<UUID, Function<String, Boolean>> chatInputs = new ConcurrentHashMap<>();

    public void register(Player player, Function<String, Boolean> function) {
        this.chatInputs.put(player.getUniqueId(), function);
    }

    public void removeChatInput(Player player) {
        this.chatInputs.remove(player.getUniqueId());
    }

    public boolean hasChatInput(Player player) {
        return this.chatInputs.containsKey(player.getUniqueId());
    }

    // Return false if player had no chat input.
    public boolean handleChatInput(Player player, String arg) {
        Function<String, Boolean> function = this.chatInputs.get(player.getUniqueId());
        if(function == null) return false;

        if(arg.equalsIgnoreCase("cancel")) {
            this.chatInputs.remove(player.getUniqueId());
            player.sendMessage(Component.text("Your input has been cancelled.", NamedTextColor.RED));
            return true;
        }

        boolean wasSuccessful = function.apply(arg);
        if(wasSuccessful) {
            this.chatInputs.remove(player.getUniqueId());
        } else {
            player.sendMessage(Component.text("You have submitted an invalid input. Try again.", NamedTextColor.RED));
        }

        return true;
    }
}
