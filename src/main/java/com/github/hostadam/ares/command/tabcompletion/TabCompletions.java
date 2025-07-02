package com.github.hostadam.ares.command.tabcompletion;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TabCompletions {

    TabCompletionMapper[] value();
}
