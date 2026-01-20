package io.github.hostadam.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AresCommand {

    String name() default "";
    String[] aliases();
    String description() default "No description provided";
    String usage() default "";
    String permission() default "";
}
