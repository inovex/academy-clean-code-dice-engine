package de.inovex.cleancode.diceengine.probability;

import java.util.function.BiFunction;

public record ResultCategory(int value, Operator operator) {

    public static enum Operator {

        EQUAL,
        LESS,
        LESS_OR_EQUAL,
        GREATER,
        GREATER_OR_EQUAL;
    }

    public static ResultCategory eq(int value) {

        return new ResultCategory(value, Operator.EQUAL);
    }

    public static ResultCategory lt(int value) {

        return new ResultCategory(value, Operator.LESS);
    }

    public static ResultCategory le(int value) {

        return new ResultCategory(value, Operator.LESS_OR_EQUAL);
    }

    public static ResultCategory gt(int value) {

        return new ResultCategory(value, Operator.GREATER);
    }

    public static ResultCategory ge(int value) {

        return new ResultCategory(value, Operator.GREATER_OR_EQUAL);
    }

}
