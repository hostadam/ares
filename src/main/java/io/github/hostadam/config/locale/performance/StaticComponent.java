package io.github.hostadam.config.locale.performance;

import io.github.hostadam.config.locale.PlaceholderProvider;
import net.kyori.adventure.text.Component;

public record StaticComponent(Component component) implements ComponentVariant {

    @Override
    public Component resolve(PlaceholderProvider provider) {
        return component;
    }
}
