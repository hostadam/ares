package io.github.hostadam.config.locale.performance;

import io.github.hostadam.config.locale.PlaceholderProvider;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;

import java.util.ArrayList;
import java.util.List;

public record DynamicComponent(List<ComponentVariant> parts) implements ComponentVariant {

    @Override
    public Component resolve(PlaceholderProvider provider) {
        List<Component> components = new ArrayList<>(this.parts.size());
        for(ComponentVariant variant : this.parts) {
            components.add(variant.resolve(provider));
        }

        return Component.join(JoinConfiguration.noSeparators(), components);
    }
}
