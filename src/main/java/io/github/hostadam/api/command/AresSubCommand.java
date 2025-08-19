package io.github.hostadam.api.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AresSubCommand {

    String parent() default "";
    String[] labels();
    String description();
    String usage() default "";
    String permission() default "";
}
