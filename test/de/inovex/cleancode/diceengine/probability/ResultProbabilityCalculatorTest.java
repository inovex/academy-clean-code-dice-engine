package de.inovex.cleancode.diceengine.probability;

import static org.assertj.core.api.Assertions.assertThat;


import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import de.inovex.cleancode.diceengine.Dice;

public class ResultProbabilityCalculatorTest {

    private final ResultProbabilityCalculator calculator = new ResultProbabilityCalculator();

    private Dice oneEightSidedDice = Dice.define().withSides(8).create();
    private Dice oneFourSidedDice = Dice.define().withSides(4).create();
    private Dice twoThreeSidedDice = Dice.define().withDice(2).withSides(3).create();

    @Test
    void noDiceNoSpice() {

        IntStream.rangeClosed(-17, 23).forEach(v ->
            Stream.<ResultCategoryCreator>of(
                    ResultCategory::eq,
                    ResultCategory::lt,
                    ResultCategory::le,
                    ResultCategory::gt,
                    ResultCategory::ge)
                .map(op -> op.apply(v))
                .forEach(r -> assertThat(calculator.probability(r)) // no dice given
                    .describedAs("result %s %d", r.operator().name(), r.value())
                    .isEqualTo(0.0))
        );
    }

    @Test
    public void calculatesProbabilityOfEqualResult() {

        IntStream.of(-1, 0, 9, 100)
            .mapToObj(ResultCategory::eq)
            .forEach(result -> assertThat(calculator.probability(result, oneEightSidedDice))
                .describedAs("result %s %d", result.operator().name(), result.value())
                .isEqualTo(0.0));

        IntStream.rangeClosed(1, 8)
            .mapToObj(ResultCategory::eq)
            .forEach(result -> assertThat(calculator.probability(result, oneEightSidedDice))
                .describedAs("result %s %d", result.operator().name(), result.value())
                .isEqualTo(0.125));
    }

    @Test
    public void calculatesProbabilityOfLessResult() {

        IntStream.of(-1, 0, 1)
            .mapToObj(ResultCategory::lt)
            .forEach(result -> assertThat(calculator.probability(result, oneEightSidedDice))
                .describedAs("result %s %d", result.operator().name(), result.value())
                .isEqualTo(0.0));

        IntStream.rangeClosed(2, 8)
            .mapToObj(ResultCategory::lt)
            .forEach(result -> assertThat(calculator.probability(result, oneEightSidedDice))
                .describedAs("result %s %d", result.operator().name(), result.value())
                .isEqualTo((result.value() - 1) / (double) 8));

        IntStream.of(9, 100)
            .mapToObj(ResultCategory::lt)
            .forEach(result -> assertThat(calculator.probability(result, oneEightSidedDice))
                .describedAs("result %s %d", result.operator().name(), result.value())
                .isEqualTo(1.0));
    }

    @Test
    public void calculatesProbabilityOfLessOrEqualResult() {

        IntStream.of(-1, 0)
            .mapToObj(ResultCategory::le)
            .forEach(result -> assertThat(calculator.probability(result, oneEightSidedDice))
                .describedAs("result %s %d", result.operator().name(), result.value())
                .isEqualTo(0.0));

        IntStream.rangeClosed(1, 7)
            .mapToObj(ResultCategory::le)
            .forEach(result -> assertThat(calculator.probability(result, oneEightSidedDice))
                .describedAs("result %s %d", result.operator().name(), result.value())
                .isEqualTo(result.value() / (double) 8));

        IntStream.of(8, 9, 100)
            .mapToObj(ResultCategory::le)
            .forEach(result -> assertThat(calculator.probability(result, oneEightSidedDice))
                .describedAs("result %s %d", result.operator().name(), result.value())
                .isEqualTo(1.0));
    }

    @Test
    public void calculatesProbabilityOfGreaterResult() {

        IntStream.of(-1, 0)
            .mapToObj(ResultCategory::gt)
            .forEach(result -> assertThat(calculator.probability(result, oneEightSidedDice))
                .describedAs("result %s %d", result.operator().name(), result.value())
                .isEqualTo(1.0));

        IntStream.rangeClosed(1, 7)
            .mapToObj(ResultCategory::gt)
            .forEach(result -> assertThat(calculator.probability(result, oneEightSidedDice))
                .describedAs("result %s %d", result.operator().name(), result.value())
                .isEqualTo(1.0 - (result.value() / (double) 8)));

        IntStream.of(8, 9, 100)
            .mapToObj(ResultCategory::gt)
            .forEach(result -> assertThat(calculator.probability(result, oneEightSidedDice))
                .describedAs("result %s %d", result.operator().name(), result.value())
                .isEqualTo(0.0));
    }

    @Test
    public void calculatesProbabilityOfGreaterOrEqualResult() {

        IntStream.of(-1, 0, 1)
            .mapToObj(ResultCategory::ge)
            .forEach(result -> assertThat(calculator.probability(result, oneEightSidedDice))
                .describedAs("result %s %d", result.operator().name(), result.value())
                .isEqualTo(1.0));

        IntStream.rangeClosed(2, 8)
            .mapToObj(ResultCategory::ge)
            .forEach(result -> assertThat(calculator.probability(result, oneEightSidedDice))
                .describedAs("result %s %d", result.operator().name(), result.value())
                .isEqualTo(1.0 - ((result.value() - 1) / (double) 8)));

        IntStream.of(9, 100)
            .mapToObj(ResultCategory::ge)
            .forEach(result -> assertThat(calculator.probability(result, oneEightSidedDice))
                .describedAs("result %s %d", result.operator().name(), result.value())
                .isEqualTo(0.0));
    }

    @Test
    public void calculatesProbabilityOfEqualResultForMultipleDice() {

        IntStream.of(-1, 0, 1, 2, 11, 35)
            .mapToObj(ResultCategory::eq)
            .forEach(result -> assertThat(calculator.probability(result, oneFourSidedDice, twoThreeSidedDice))
                .describedAs("result %s %d", result.operator().name(), result.value())
                .isEqualTo(0.0));

        double overall = 36.0;
        assertThat(calculator.probability(ResultCategory.eq(3), oneFourSidedDice, twoThreeSidedDice)).isEqualTo(1 / overall);
        assertThat(calculator.probability(ResultCategory.eq(4), oneFourSidedDice, twoThreeSidedDice)).isEqualTo(3 / overall);
        assertThat(calculator.probability(ResultCategory.eq(5), oneFourSidedDice, twoThreeSidedDice)).isEqualTo(6 / overall);
        assertThat(calculator.probability(ResultCategory.eq(6), oneFourSidedDice, twoThreeSidedDice)).isEqualTo(8 / overall);
        assertThat(calculator.probability(ResultCategory.eq(7), oneFourSidedDice, twoThreeSidedDice)).isEqualTo(8 / overall);
        assertThat(calculator.probability(ResultCategory.eq(8), oneFourSidedDice, twoThreeSidedDice)).isEqualTo(6 / overall);
        assertThat(calculator.probability(ResultCategory.eq(9), oneFourSidedDice, twoThreeSidedDice)).isEqualTo(3 / overall);
        assertThat(calculator.probability(ResultCategory.eq(10), oneFourSidedDice, twoThreeSidedDice)).isEqualTo(1 / overall);
    }

    @FunctionalInterface
    private interface ResultCategoryCreator extends Function<Integer, ResultCategory> { }
}
