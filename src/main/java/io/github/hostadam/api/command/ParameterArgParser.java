package io.github.hostadam.api.command;

import java.util.Optional;

@FunctionalInterface
public interface ParameterArgParser<T> {

    Optional<T> apply(String arg);
}
