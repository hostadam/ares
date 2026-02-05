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

import com.github.hostadam.board.BoardHandler;
import com.github.hostadam.command.CommandHolder;
import com.github.hostadam.command.CommandNode;
import com.github.hostadam.command.dispatch.CommandDispatcher;
import com.github.hostadam.command.wrapper.BukkitCommandNode;
import com.github.hostadam.persistence.config.Config;
import com.github.hostadam.persistence.config.ConfigFile;
import com.github.hostadam.persistence.config.message.MessageConfig;
import com.github.hostadam.utilities.InputHandler;
import com.github.hostadam.utilities.item.ItemFactory;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.function.Function;

public final class AresPlugin extends JavaPlugin implements Ares {

    private static Ares API;

    private ItemFactory itemFactory;
    private BoardHandler boardHandler;
    private InputHandler inputHandler;
    private CommandDispatcher dispatcher;

    @Override
    public void onEnable() {
        this.itemFactory = new ItemFactory();
        this.boardHandler = new BoardHandler(this);
        this.inputHandler = new InputHandler();
        this.dispatcher = new CommandDispatcher();

        this.getServer().getPluginManager().registerEvents(new AresEvents(this.boardHandler, this.inputHandler), this);
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        API = this;
    }

    @Override
    public MessageConfig createMessageConfig(ConfigFile file) {
        return createConfig(file, MessageConfig.class);
    }

    @Override
    public <T extends Config> T createConfig(ConfigFile file, Class<T> clazz) {
        try {
            Constructor<T> constructor = clazz.getConstructor(ConfigFile.class);
            T instance = constructor.newInstance(file);
            instance.load();
            return instance;
        } catch (InstantiationException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Failed to create config.", e);
        }
    }

    @Override
    public <T extends CommandHolder> void registerCommands(T holder) {
        List<CommandNode> nodes = holder.registerCommands();
        if(nodes == null || nodes.isEmpty()) return;

        for(CommandNode node : nodes) {
            BukkitCommandNode bukkitCommand = new BukkitCommandNode(this.dispatcher, node);
            this.getServer().getCommandMap().register(node.primaryName(), bukkitCommand);
        }
    }

    @Override
    public void startChatInput(Player player, Function<String, Boolean> function) {
        this.inputHandler.register(player, function);
    }

    @Override
    public BoardHandler scoreboard() {
        return this.boardHandler;
    }

    @Override
    public ItemFactory itemFactory() {
        return this.itemFactory;
    }

    public static Ares api() {
        return API;
    }
}
