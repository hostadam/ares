package io.github.hostadam.config.locale.performance;

import io.github.hostadam.config.locale.PlaceholderProvider;
import net.kyori.adventure.text.Component;

public record PlaceholderComponent(String key) implements CachedComponentSegment { }
