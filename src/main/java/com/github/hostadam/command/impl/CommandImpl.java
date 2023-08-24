package com.github.hostadam.command.impl;

import com.github.hostadam.command.Command;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.lang.reflect.Method;

@Getter
@AllArgsConstructor
public class CommandImpl {

    private Command command;
    private Method method;
    private Object object;
}
