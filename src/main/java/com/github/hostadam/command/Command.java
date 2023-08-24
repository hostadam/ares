package com.github.hostadam.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Command {

    String[] labels();
    String description();
    String usage() default "";
    String permission() default "";
    int requiredArgs() default 0;
    CommandTarget target() default CommandTarget.ALL;
}
