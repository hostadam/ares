package io.github.hostadam.config.locale;

import net.kyori.adventure.text.Component;

@FunctionalInterface
public interface PlaceholderProvider {

    Component get(String key);

    static PlaceholderProvider empty() {
        return key -> null;
    }

    static PlaceholderProvider create(String providedKey, Component value) {
        return key -> key.equals(providedKey) ? value : null;
    }

    default PlaceholderProvider and(PlaceholderProvider next) {
        return key -> {
            Component result = this.get(key);
            return result != null ? result : next.get(key);
        };
    }

    default PlaceholderProvider and(String providedKey, Component value) {
        return this.and(PlaceholderProvider.create(providedKey, value));
    }
}
