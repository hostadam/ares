package io.github.hostadam.api;

import java.util.function.Predicate;

public record ChatInput(Predicate<String> reader, boolean cancellable) {

    public ChatInput(Predicate<String> reader) {
         this(reader, true);
    }
}
