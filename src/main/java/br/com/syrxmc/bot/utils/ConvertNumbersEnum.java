package br.com.syrxmc.bot.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ConvertNumbersEnum {

    ONE(1, ":one:"),
    TWO(2, ":two:"),
    THREE(3, ":three:"),
    FOUR(4, ":four:"),
    FIVE(5, ":five:"),;

    private final int value;

    private final String description;
}
