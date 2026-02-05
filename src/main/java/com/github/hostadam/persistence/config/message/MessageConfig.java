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

package com.github.hostadam.persistence.config.message;

import com.github.hostadam.persistence.config.Config;
import com.github.hostadam.persistence.config.ConfigFile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class MessageConfig extends Config {

    public static final Pattern PLACEHOLDER_PATTERN =
            Pattern.compile("\\{(\\w+)}");
    private static final ParsedMessage PARSED_MESSAGE_ERROR = ParsedMessage.of(
            Component.text("An unexpected error occurred while fetching a message. Please contact an administrator.", NamedTextColor.RED)
    );

    private final Map<String, ParsedMessage> messages = new ConcurrentHashMap<>();

    public MessageConfig(ConfigFile file) {
        super(file);
    }

    public Message message(String path) {
        ParsedMessage message = this.messages.getOrDefault(path.toLowerCase(), PARSED_MESSAGE_ERROR);
        return new Message(message);
    }

    @Override
    public void load() {
        this.messages.clear();

        YamlConfiguration config = this.file.get();
        for(String path : config.getKeys(true)) {
            List<String> entries = config.isList(path) ? config.getStringList(path)
                    : config.isString(path) ? List.of(Objects.requireNonNull(config.getString(path))) : null;
            if(entries == null) continue;
            this.messages.put(path, ParsedMessage.of(entries));
        }
    }
}
