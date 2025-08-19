package io.github.hostadam.config.locale.performance;

import io.github.hostadam.config.locale.PlaceholderProvider;
import net.kyori.adventure.text.Component;

public sealed interface CachedComponentSegment permits StaticComponent, PlaceholderComponent { }
