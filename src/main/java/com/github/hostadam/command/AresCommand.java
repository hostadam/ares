package com.github.hostadam.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AresCommand {

    String[] labels();
    String description();
    String usage() default "";
    String permission() default "";
    int requiredArgs() default 0;
}
