package com.github.hostadam.ares.command.context;

import java.util.Optional;

@FunctionalInterface
public interface ParameterArgParser<T> {

    Optional<T> apply(String arg);
}
