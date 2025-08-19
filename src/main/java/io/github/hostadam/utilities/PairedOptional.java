package io.github.hostadam.utilities;

import java.util.function.BiConsumer;

public class PairedOptional<F, S> {

    private final F first;
    private final S second;

    private PairedOptional(F first, S second) {
        this.first = first;
        this.second = second;
    }

    public void ifPresent(BiConsumer<F, S> consumer) {
        if(this.isPresent()) {
            consumer.accept(first, second);
        }
    }

    public F getFirst() {
        return this.first;
    }

    public S getSecond() {
        return this.second;
    }

    public boolean isPresent() {
        return first != null && second != null;
    }

    public boolean isEmpty() {
        return first == null && second == null;
    }

    public static <F, S> PairedOptional<F, S> of(F first, S second) {
        return new PairedOptional<>(first, second);
    }

    public static <F, S> PairedOptional<F, S> empty() {
        return new PairedOptional<>(null, null);
    }
}
