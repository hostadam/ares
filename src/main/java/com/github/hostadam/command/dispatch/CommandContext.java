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

import lombok.Getter;
import org.bukkit.command.CommandSender;

import java.util.Map;

public class CommandContext {

    @Getter
    private final CommandSender source;
    private final Map<String, Object> parsedArgs;

    public CommandContext(CommandSender source, Map<String, Object> parsedArgs) {
        this.source = source;
        this.parsedArgs = parsedArgs;
    }

    public <T> T getArgument(String key, Class<T> clazz) {
        Object value = this.parsedArgs.get(key);
        return clazz.isInstance(value) ? clazz.cast(value) : null;
    }

    public <T> T getArgument(String key, Class<T> clazz, T defaultValue) {
        T argument = this.getArgument(key, clazz);
        return argument == null ? defaultValue : argument;
    }

    public <S extends CommandSender> S getSourceAs(Class<S> sourceClass) {
        return sourceClass.isInstance(this.source) ? sourceClass.cast(this.source) : null;
    }
}
