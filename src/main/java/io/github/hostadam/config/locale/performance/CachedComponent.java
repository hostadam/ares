package io.github.hostadam.config.locale.performance;

import java.util.List;

public record CachedComponent(List<CachedComponentSegment> variants, boolean hasPlaceholders, boolean needsRoundtrip) { }
