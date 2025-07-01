package com.github.hostadam.ares.command.experimental;

import java.util.Optional;

@FunctionalInterface
public interface ParameterArgParser<T> {

    Optional<T> apply(String arg);
}
