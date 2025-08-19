package io.github.hostadam.config.locale;

import io.github.hostadam.config.ConfigFile;
import io.github.hostadam.config.locale.performance.*;
import io.github.hostadam.utilities.PaperUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.TextComponent;
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

    private ComponentVariant parseComponent(String input) {
        Component root = PaperUtils.stringToComponent(input); // parse MiniMessage once
        List<ComponentLike> parts = new ArrayList<>();
        collectParts(root, parts);
        return new DynamicComponent(parts);
    }

    private void collectParts(Component comp, List<ComponentLike> parts) {
        if (comp instanceof TextComponent text) {
            String content = text.content();
            int last = 0;
            for (int i = 0; i < content.length(); i++) {
                if (content.charAt(i) == '{') {
                    int end = content.indexOf('}', i);
                    if (end > i) {
                        if (i > last) parts.add(new StaticComponent(Component.text(content.substring(last, i), text.decorations(), text.color())));
                        parts.add(new PlaceholderComponent(content.substring(i + 1, end)));
                        i = end;
                        last = i + 1;
                    }
                }
            }

            if(last < content.length()) {
                parts.add(new StaticComponent(Component.text(content.substring(last), text.color(), text.decorations())));
            }
        }

        for(Component child : comp.children()) {
            collectParts(child, parts);
        }
    }

    private CachedComponent parseList(List<String> list) {
        if(list.isEmpty()) return null;

        List<ComponentVariant> components = new ArrayList<>(list.size());
        for(String string : list) {
            ComponentVariant variant = this.parseComponent(string);
            components.add(variant);
        }

        return new CachedComponent(components);
    }

    private CachedComponent parseMessage(String string) {
        if(string == null) return null;
        ComponentVariant variant = this.parseComponent(string);
        return new CachedComponent(List.of(variant));
    }

    public Component resolve(String key, PlaceholderProvider provider) {
        CachedComponent cache = this.cachedComponents.get(key);
        return cache != null ? cache.resolve(provider) : Component.empty();
    }
}
