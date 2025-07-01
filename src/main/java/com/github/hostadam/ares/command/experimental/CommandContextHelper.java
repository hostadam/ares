package com.github.hostadam.ares.command.experimental;

import com.google.common.primitives.Doubles;
import com.google.common.primitives.Ints;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class CommandContextHelper {

    private final Map<Class<?>, ParameterArgParser<?>> argParsers;

    public CommandContextHelper() {
        this.argParsers = new ConcurrentHashMap<>();
    }

    private void registerDefaultParsers() {
        this.registerParser(String.class, Optional::of);
        this.registerParser(Integer.class, arg -> {
            Integer value = Ints.tryParse(arg);
            return Optional.ofNullable(value);
        });

        this.registerParser(Double.class, arg -> {
            Double value = Doubles.tryParse(arg);
            return Optional.ofNullable(value);
        });
    }

    public <T> void registerParser(Class<T> clazz, ParameterArgParser<T> parser) {
        this.argParsers.put(clazz, parser);
    }

    public <T> Optional<T> parse(Class<?> type, String value) {
        if(!this.argParsers.containsKey(type)) return Optional.empty();
        ParameterArgParser<T> parameterArgParser = (ParameterArgParser<T>) this.argParsers.get(type);
        return parameterArgParser.apply(value);
    }
}
