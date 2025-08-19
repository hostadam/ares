package io.github.hostadam.config.locale;

import io.github.hostadam.config.ConfigFile;
import io.github.hostadam.config.locale.performance.CachedComponent;
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

    private CachedComponent parseList(List<String> list) {
        if(list.isEmpty()) return null;

        List<Component> components = new ArrayList<>();
        boolean hasPlaceholders = false;

        for(String string : list) {
            Matcher matcher = PLACEHOLDER_PATTERN.matcher(string);
            if(matcher.find()) {
                hasPlaceholders = true;
            }

            components.add(PaperUtils.stringToComponent(string));
        }

        Component joinedComponent = Component.join(JoinConfiguration.separator(Component.newline()), components);
        boolean needsExtraRoundtrip = hasInnerPlaceholders(joinedComponent);
        return new CachedComponent(joinedComponent, hasPlaceholders, needsExtraRoundtrip);
    }

    private CachedComponent parseMessage(String miniMessageString) {
        if(miniMessageString == null || miniMessageString.isEmpty()) return null;
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(miniMessageString);
        boolean hasPlaceholder = matcher.find();

        Component component = PaperUtils.stringToComponent(miniMessageString);
        boolean needsExtraRoundtrip = hasInnerPlaceholders(component);
        return new CachedComponent(component, hasPlaceholder, needsExtraRoundtrip);
    }

    public Component resolve(String key, PlaceholderProvider provider) {
        CachedComponent cache = this.cachedComponents.get(key);
        if(cache == null) return Component.empty();
        return getComponent(provider, cache);
    }

    private Component getComponent(PlaceholderProvider provider, CachedComponent cache) {
        Component component = cache.component();
        if(!cache.containsPlaceholders()) {
            return component;
        }

        component = component.replaceText(builder -> builder
                .match(PLACEHOLDER_PATTERN)
                .replacement((matchResult, builder1) -> {
                    String key = matchResult.group(1);
                    Component value = provider.get(key);
                    return value != null ? value : Component.text(matchResult.group());
                })
        );

        return cache.needsExtraRoundtrip() ? this.recursiveResolving(component, provider) : component;
    }

    private Component recursiveResolving(Component component, PlaceholderProvider provider) {
        ClickEvent clickEvent = component.clickEvent();
        if(clickEvent != null && clickEvent.payload() instanceof ClickEvent.Payload.Text text) {
            String value = text.value();
            Matcher matcher = PLACEHOLDER_PATTERN.matcher(value);
            if(matcher.find()) {
                String replacedValue = matcher.replaceAll(matchResult -> {
                    String key = matchResult.group(1);
                    Component replacement = provider.get(key);
                    return replacement instanceof TextComponent textComponent ? textComponent.content() : matchResult.group();
                });

                return component.clickEvent(ClickEvent.clickEvent(clickEvent.action(), replacedValue));
            }
        }

        return component;
    }

    private boolean hasInnerPlaceholders(Component component) {
        ClickEvent clickEvent = component.clickEvent();
        return clickEvent != null && clickEvent.payload() instanceof ClickEvent.Payload.Text text && PLACEHOLDER_PATTERN.matcher(text.value()).find();
    }
}
