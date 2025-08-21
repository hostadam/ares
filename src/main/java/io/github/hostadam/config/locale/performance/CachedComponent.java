package io.github.hostadam.config.locale.performance;

import net.kyori.adventure.text.Component;

public record CachedComponent(Component component, boolean containsPlaceholders, boolean needsExtraRoundtrip) { }