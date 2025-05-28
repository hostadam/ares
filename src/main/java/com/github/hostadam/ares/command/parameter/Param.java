package com.github.hostadam.ares.command.parameter;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Param {

    boolean optional() default false;
    boolean errorIfEmpty() default true;
}
