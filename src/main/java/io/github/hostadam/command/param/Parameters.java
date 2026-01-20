package io.github.hostadam.command.param;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Parameters {

    ParamMapper[] value();
}
