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

package com.github.hostadam;

import com.google.common.base.Preconditions;
import com.github.hostadam.board.BoardHandler;
import com.github.hostadam.command.CommandHolder;
import com.github.hostadam.persistence.config.Config;
import com.github.hostadam.persistence.config.ConfigFile;
import com.github.hostadam.persistence.config.message.MessageConfig;
import com.github.hostadam.utilities.item.ItemFactory;
import org.bukkit.entity.Player;

import java.util.function.Function;

public sealed interface Ares permits AresPlugin {

    static Ares api() {
        return Holder.INSTANCE;
    }

    final class Holder {
        private static Ares INSTANCE;

        static void bootstrap(Ares instance) {
            Preconditions.checkArgument(INSTANCE != null, "Ares has already been bootstrapped");
            Preconditions.checkNotNull(instance, "Ares instance must not be null");
            INSTANCE = instance;
        }
    }

    /* Shortcut method for creating a MessageConfig with automatic deserialization of in-game messages */
    default MessageConfig createMessageConfig(ConfigFile file) {
        return this.createConfig(file, MessageConfig.class);
    }

    /**
     * Create a Config object for automatic serialization and deserialization of fields
     *
     * @param configFile - the file to read and write to
     * @param configClass - the class of the intended Config object
     * @return the created config class
     */
    <T extends Config> T createConfig(ConfigFile configFile, Class<T> configClass);



    <T extends CommandHolder> void registerCommands(T holder);

    /*
    Start a chat input for a given player.
     */
    void startChatInput(Player player, Function<String, Boolean> function);

    /*
    Get the current scoreboard handler.
     */
    BoardHandler scoreboard();

    /*
    Get the current universal item factory.
     */
    ItemFactory itemFactory();

}
