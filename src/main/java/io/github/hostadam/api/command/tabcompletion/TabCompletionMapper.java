package io.github.hostadam.api.command.tabcompletion;

import java.lang.annotation.*;

@Repeatable(TabCompletions.class)
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TabCompletionMapper {

    String key();
    Class<?> mappedClass();
}
