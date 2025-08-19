package io.github.hostadam.config.locale.performance;

import net.kyori.adventure.text.Component;

import java.util.List;

public record CachedComponent(Component component, List<String> placeholders, boolean needsExtraRoundtrip) {

    public boolean hasPlaceholders() {
        return !this.placeholders.isEmpty();
    }

}
