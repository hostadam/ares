package com.github.hostadam.ares.utils.internals;

import org.jetbrains.annotations.ApiStatus;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@ApiStatus.Internal
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE, ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.FIELD, ElementType.PACKAGE})
public @interface RevisionRequired {

    String since();
    boolean partial();
    String description();
}
