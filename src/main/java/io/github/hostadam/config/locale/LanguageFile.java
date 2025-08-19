package io.github.hostadam.config.locale;

import io.github.hostadam.config.ConfigFile;
import io.github.hostadam.config.locale.performance.CachedComponent;
import io.github.hostadam.config.locale.performance.ParsedComponent;
import io.github.hostadam.utilities.PaperUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LanguageFile extends ConfigFile {

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{(\\w+)}");
    private Map<String, CachedComponent> cachedComponents;

    public LanguageFile(JavaPlugin plugin) {
        super(plugin, "lang");
    }

    public int size() {
        return this.cachedComponents.keySet().size();
    }

    @Override
    public boolean contains(@NotNull String path) {
        return this.cachedComponents.containsKey(path);
    }

    @Override
    public void load() {
        super.load();
        this.populate();
    }

    private void populate() {
        this.cachedComponents = new ConcurrentHashMap<>();
        for(String key : this.getKeys(true)) {
            if(this.isList(key)) {
                CachedComponent component = this.parseList(this.getStringList(key));
                if(component == null) continue;
                this.cachedComponents.put(key, component);
            } else if(this.isString(key)) {
                CachedComponent component = this.parseMessage(this.getString(key));
                if(component == null) continue;
                this.cachedComponents.put(key, component);
            }
        }
    }

    private ParsedComponent parseComponent(String string) {
        List<Component> components = new ArrayList<>();
        List<String> placeholders = new ArrayList<>();

        int index = 0;
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(string);

        while(matcher.find()) {
            String before = string.substring(index, matcher.start());
            if(!before.isEmpty()) {
                components.add(PaperUtils.stringToComponent(before));
            }

            String key = matcher.group(1);
            placeholders.add(key);
            components.add(Component.text("PLACEHOLDER:" + key));

            index = matcher.end();
        }

        if(index < string.length()) components.add(PaperUtils.stringToComponent(string.substring(index)));
        return new ParsedComponent(components, placeholders);
    }

    private CachedComponent parseList(List<String> list) {
        if(list.isEmpty()) return null;

        List<Component> components = new ArrayList<>();
        List<String> placeholders = new ArrayList<>();

        for(String string : list) {
            ParsedComponent component = this.parseComponent(string);
            components.addAll(component.parts());
            placeholders.addAll(component.placeholders());
        }

        Component joinedComponent = Component.join(JoinConfiguration.separator(Component.newline()), components);
        boolean needsExtraRoundtrip = hasInnerPlaceholders(joinedComponent);
        return new CachedComponent(joinedComponent, placeholders, needsExtraRoundtrip);
    }

    private CachedComponent parseMessage(String miniMessageString) {
        if(miniMessageString == null || miniMessageString.isEmpty()) return null;
        ParsedComponent parsedComponent = this.parseComponent(miniMessageString);
        Component component = Component.join(JoinConfiguration.noSeparators(), parsedComponent.parts());
        boolean needsExtraRoundtrip = hasInnerPlaceholders(component);
        return new CachedComponent(component, parsedComponent.placeholders(), needsExtraRoundtrip);
    }

    public Component resolve(String key, PlaceholderProvider provider) {
        CachedComponent cache = this.cachedComponents.get(key);
        if(cache == null) return Component.empty();
        return getComponent(provider, cache);
    }

    private Component getComponent(PlaceholderProvider provider, CachedComponent cache) {
        if(!cache.hasPlaceholders()) return cache.component();
        Component result = replacePlaceholders(cache.component(), cache.placeholders(), provider);
        return cache.needsExtraRoundtrip() ? this.recursiveResolving(result, provider) : result;
    }

    private Component replacePlaceholders(Component component, List<String> placeholders, PlaceholderProvider provider) {
        Component current = component;
        for(String key : placeholders) {
            Component value = provider.get(key);
            if(value == null) value = Component.text("{" + key + "}");
            current = replacePlaceholderMarker(current, key, value);
        }

        return current;
    }

    private Component replacePlaceholderMarker(Component component, String key, Component replacement) {
        if(component instanceof TextComponent text) {
            if(text.content().equals("PLACEHOLDER:" + key)) return replacement;
            return text;
        }

        if(component.children().isEmpty()) return component;

        List<Component> newChildren = component.children().stream()
                .map(child -> replacePlaceholderMarker(child, key, replacement))
                .toList();

        return component.children(newChildren);
    }

    private Component recursiveResolving(Component component, PlaceholderProvider provider) {
        ClickEvent clickEvent = component.clickEvent();
        if (clickEvent != null && clickEvent.payload() instanceof ClickEvent.Payload.Text text) {
            String value = text.value();
            for (String key : cachedComponents.keySet()) {
                Component replacement = provider.get(key);
                if (replacement instanceof TextComponent tc && value.contains("{" + key + "}")) {
                    value = value.replace("{" + key + "}", tc.content());
                }
            }
            return component.clickEvent(ClickEvent.clickEvent(clickEvent.action(), value));
        }
        return component;
    }

    private boolean hasInnerPlaceholders(Component component) {
        ClickEvent clickEvent = component.clickEvent();
        return clickEvent != null && clickEvent.payload() instanceof ClickEvent.Payload.Text text && PLACEHOLDER_PATTERN.matcher(text.value()).find();
    }
}
