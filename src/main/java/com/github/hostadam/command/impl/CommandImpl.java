package com.github.hostadam.command.impl;

import com.github.hostadam.command.AresCommand;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.lang.reflect.Method;

@Getter
@AllArgsConstructor
public class CommandImpl {

    private AresCommand command;
    private Method method;
    private Object object;
}
