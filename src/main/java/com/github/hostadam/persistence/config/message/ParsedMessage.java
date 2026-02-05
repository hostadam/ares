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

import com.github.hostadam.utilities.AdventureUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.event.ClickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

public record ParsedMessage(
        Component baseComponent,
        boolean hasPlaceholders,
        boolean hasNestedPlaceholders
) {

    public boolean needsResolution() {
        return this.hasPlaceholders || this.hasNestedPlaceholders();
    }

    public static ParsedMessage of(Component text) {
        return new ParsedMessage(text, false, false);
    }

    public static ParsedMessage of(List<String> strings) {
        List<Component> components = new ArrayList<>();
        boolean hasPlaceholders = false, hasNestedPlaceholders = false;

        for(String text : strings) {
            if(!hasPlaceholders) {
                Matcher matcher = MessageConfig.PLACEHOLDER_PATTERN.matcher(text);
                if(matcher.find()) {
                    hasPlaceholders = true;
                }
            }

            Component component = AdventureUtils.parseMiniMessage(text);
            ClickEvent event = component.clickEvent();
            if(!hasNestedPlaceholders && event != null && event.payload() instanceof ClickEvent.Payload.Text payload && MessageConfig.PLACEHOLDER_PATTERN.matcher(payload.value()).find()) {
                hasNestedPlaceholders = true;
            }

            components.add(component);
        }

        return new ParsedMessage(Component.join(JoinConfiguration.separator(Component.newline()), components), hasPlaceholders, hasNestedPlaceholders);
    }
}
