package io.github.hostadam.config.lang;

import io.github.hostadam.utilities.PaperUtils;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.event.ClickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParsedMessage {

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{(\\w+)}");

    @Getter
    private final Component baseComponent;
    private final boolean hasPlaceholders;
    private final boolean hasNestedPlaceholders;

    public ParsedMessage(Component text) {
        this.baseComponent = text;
        this.hasPlaceholders = this.hasNestedPlaceholders = false;
    }

    public ParsedMessage(List<String> textList) {
        List<Component> components = new ArrayList<>();
        boolean hasPlaceholders = false, hasNestedPlaceholders = false;

        for(String text : textList) {
            if(!hasPlaceholders) {
                Matcher matcher = PLACEHOLDER_PATTERN.matcher(text);
                if(matcher.find()) {
                    hasPlaceholders = true;
                }
            }

            Component component = PaperUtils.stringToComponent(text);
            ClickEvent event = component.clickEvent();
            if(!hasNestedPlaceholders && event != null && event.payload() instanceof ClickEvent.Payload.Text payload && PLACEHOLDER_PATTERN.matcher(payload.value()).find()) {
                hasNestedPlaceholders = true;
            }

            components.add(component);
        }

        this.baseComponent = Component.join(JoinConfiguration.separator(Component.newline()), components);
        this.hasPlaceholders = hasPlaceholders;
        this.hasNestedPlaceholders = hasNestedPlaceholders;
    }

    public boolean needsResolution() {
        return this.hasPlaceholders || this.hasNestedPlaceholders();
    }

    public boolean hasNestedPlaceholders() {
        return this.hasNestedPlaceholders;
    }

    public static Pattern getPlaceholderPattern() {
        return PLACEHOLDER_PATTERN;
    }
}
