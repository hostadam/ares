package io.github.hostadam.config.locale.performance;

import net.kyori.adventure.text.Component;

import java.util.List;

public record ParsedComponent(List<Component> parts, List<String> placeholders) {
}
