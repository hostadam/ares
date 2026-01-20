package io.github.hostadam.command.param;

import io.github.hostadam.command.AresParameter;

import java.lang.annotation.*;

@Repeatable(Parameters.class)
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ParamMapper {

    String key();
    Class<? extends AresParameter<?>> value();
}
