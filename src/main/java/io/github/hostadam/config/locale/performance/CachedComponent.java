package io.github.hostadam.config.locale.performance;

import io.github.hostadam.config.locale.PlaceholderProvider;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;

import java.util.ArrayList;
import java.util.List;

public record CachedComponent(List<ComponentVariant> variants) {

    public Component resolve(PlaceholderProvider provider) {
        List<Component> resolved = new ArrayList<>(variants.size());
        for (ComponentVariant variant : variants) {
            resolved.add(variant.resolve(provider));
        }
        return Component.join(JoinConfiguration.newlines(), resolved);
    }
}
