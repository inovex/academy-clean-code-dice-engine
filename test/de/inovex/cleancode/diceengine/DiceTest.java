package de.inovex.cleancode.diceengine;

import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.*;


import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import de.inovex.cleancode.diceengine.Dice.DiceDefinition;

class DiceTest {

    private static final int SAMPLE_SIZE = 1000;

    @Test
    void cannotCreateDiceWithoutSides() {

        DiceDefinition diceDefinition = Dice.define();

        assertThatThrownBy(diceDefinition::create)
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void cannotCreateNegativeSidedDice() {

        DiceDefinition diceDefinition = Dice.define().withSides(-1);

        assertThatThrownBy(diceDefinition::create)
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void cannotCreateZeroSidedDice() {

        DiceDefinition diceDefinition = Dice.define().withSides(0);

        assertThatThrownBy(diceDefinition::create)
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void cannotCreateNegativeNumberOfPositiveSidedDice() {

        DiceDefinition diceDefinition = Dice.define().withSides(1).withDice(-1);

        assertThatThrownBy(diceDefinition::create)
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void canCreateSinglePositiveSidedUnmodifiedDice() {

        DiceDefinition diceDefinition = Dice.define().withSides(1);

        int unmodifiedMinimum = 1;
        Stream.of(1, 2, 3, 4, 5, 6, 8, 10, 12, 15, 20, 100, 1337, 15000, Integer.MAX_VALUE)
            .forEach(sides -> {
                Dice dice = diceDefinition.withSides(sides).create();
                assertThat(dice.minimum()).isEqualTo(unmodifiedMinimum);
                assertThat(dice.maximum()).isEqualTo(sides);
            });
    }

    @Test
    void canCreateZeroPositiveSidedDice() {

        Dice zeroDice = Dice.define().withSides(1).withDice(0).create();

        assertThat(zeroDice.minimum()).isZero();
        assertThat(zeroDice.maximum()).isZero();
    }

    @Test
    void canCreateUnmodifiedDiceOfPositiveCardinality() {

        int sides = 6;
        DiceDefinition diceDefinition = Dice.define().withSides(sides);

        Stream.of(1, 2, 5, 17, 42, 100, 500).forEach(cardinality -> {
            Dice dice = diceDefinition.withDice(cardinality).create();
            assertThat(dice.minimum()).isEqualTo(cardinality);
            assertThat(dice.maximum()).isEqualTo(sides * cardinality);
        });
    }

    @Test
    void canCreateModifiedDice() {

        int sides = 17;
        DiceDefinition diceDefinition = Dice.define()
            .withSides(sides);

        Stream.of(-27, -1, 0, 1, 2, 13, 100, 18000).forEach(modifier -> {
            diceDefinition.withMod(modifier);

            Stream.of(1, 2, 5, 17, 42, 100, 50).forEach(cardinality -> {
                Dice dice = diceDefinition.withDice(cardinality).create();
                assertThat(dice.minimum()).isEqualTo(cardinality + modifier);
                assertThat(dice.maximum()).isEqualTo(sides * cardinality + modifier);
            });

            Dice dice = diceDefinition.withDice(0).create();
            assertThat(dice.minimum()).isEqualTo(modifier);
            assertThat(dice.maximum()).isEqualTo(modifier);
        });
    }

    @Test
    void canCreateDiceWithMinimalModifier() {

        DiceDefinition diceDefinition = Dice.define()
            .withMod(Integer.MIN_VALUE);

        Stream.of(0, 1, 6, 17, 100).forEach(cardinality ->
            Stream.of(1, 2, 6, 100).forEach(sides -> {
                Dice dice = diceDefinition
                    .withSides(sides)
                    .withDice(cardinality)
                    .create();
                assertThat(dice.minimum()).isEqualTo(Integer.MIN_VALUE + cardinality);
                assertThat(dice.maximum()).isEqualTo(Integer.MIN_VALUE + cardinality * sides);
            }));

        Dice dice = diceDefinition
            .withSides(Integer.MAX_VALUE)
            .withDice(1)
            .create();
        assertThat(dice.minimum()).isEqualTo(Integer.MIN_VALUE + 1);
        assertThat(dice.maximum()).isEqualTo(-1);

        dice = diceDefinition
            .withSides(Integer.MAX_VALUE)
            .withDice(2)
            .create();
        assertThat(dice.minimum()).isEqualTo(Integer.MIN_VALUE + 2);
        assertThat(dice.maximum()).isEqualTo(Integer.MAX_VALUE - 1);

        dice = diceDefinition
            .withSides(Integer.MAX_VALUE)
            .withDice(2)
            .withMod(Integer.MIN_VALUE + 1)
            .create();
        assertThat(dice.minimum()).isEqualTo(Integer.MIN_VALUE + 3);
        assertThat(dice.maximum()).isEqualTo(Integer.MAX_VALUE);
    }

    @Test
    void canCreateDiceWithNearlyMaximalModifier() {

        DiceDefinition diceDefinition = Dice.define();

        Stream.of(0, 1, 6, 17, 100).forEach(cardinality ->
            Stream.of(1, 2, 6, 100).forEach(sides -> {
                Dice dice = diceDefinition
                    .withSides(sides)
                    .withDice(cardinality)
                    .withMod(Integer.MAX_VALUE - cardinality * sides)
                    .create();
                assertThat(dice.minimum()).isEqualTo(Integer.MAX_VALUE - cardinality * (sides - 1));
                assertThat(dice.maximum()).isEqualTo(Integer.MAX_VALUE);
            }));
    }

    @Test
    void cannotCreateDiceWithTooHighMaximalResult() {

        assertThatThrownBy(() -> Dice.define()
            .withSides(2)
            .withDice(Integer.MAX_VALUE)
            .create())
            .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> Dice.define()
            .withSides(2)
            .withDice(Integer.MAX_VALUE)
            .create())
            .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> Dice.define()
            .withSides(Integer.MAX_VALUE)
            .withMod(1)
            .create())
            .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> Dice.define()
            .withSides(1)
            .withMod(Integer.MAX_VALUE)
            .create())
            .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> Dice.define()
            .withSides(Integer.MAX_VALUE)
            .withDice(2)
            .withMod(Integer.MIN_VALUE + 2)
            .create())
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void returnsEquallyDistributedResultValuesForSingleDice() {

        Stream.of(1, 6, 17, 100).forEach(sides ->
            Stream.of(-17, 0, 1, 23).forEach(modifier -> {

                Dice dice = Dice.define()
                    .withSides(sides)
                    .withMod(modifier)
                    .create();

                List<Result> distribution = dice.resultDistribution();
                Set<Integer> values = distribution.stream()
                    .map(Result::result)
                    .collect(toSet());

                assertThat(values).hasSize(dice.sides);
                assertThat(distribution)
                    .hasSize(dice.sides)
                    .allSatisfy(result -> {
                        assertThat(result.result()).isBetween(1 + modifier, dice.sides + 1 + modifier);
                        assertThat(result.count()).isEqualTo(1);
                        assertThat(result.overall()).isEqualTo(dice.sides);
                    });
            }));
    }

    @Test
    void returnsSingleResultValueForZeroDice() {

        Dice dice = Dice.define()
            .withSides(6)
            .withDice(0)
            .create();

        assertThat(dice.resultDistribution())
            .containsExactly(new Result(0, 1, 1));
    }

    @Test
    void returnsGaussDistributedResultValuesForMultipleDice() {

        Dice twoDSix = Dice.define()
            .withDice(2)
            .withSides(6)
            .create();

        int combinationOfTwoDice = numberOfCombinations(twoDSix);
        assertThat(twoDSix.resultDistribution()).containsExactly(
            new Result(2, 1, combinationOfTwoDice),
            new Result(3, 2, combinationOfTwoDice),
            new Result(4, 3, combinationOfTwoDice),
            new Result(5, 4, combinationOfTwoDice),
            new Result(6, 5, combinationOfTwoDice),
            new Result(7, 6, combinationOfTwoDice),
            new Result(8, 5, combinationOfTwoDice),
            new Result(9, 4, combinationOfTwoDice),
            new Result(10, 3, combinationOfTwoDice),
            new Result(11, 2, combinationOfTwoDice),
            new Result(12, 1, combinationOfTwoDice)
        );

        Dice threeDFour = Dice.define()
            .withDice(3)
            .withSides(4)
            .create();

        int combinationOfThreeDice = numberOfCombinations(threeDFour);
        assertThat(threeDFour.resultDistribution()).containsExactly(
            new Result(3, 1, combinationOfThreeDice),
            new Result(4, 3, combinationOfThreeDice),
            new Result(5, 6, combinationOfThreeDice),
            new Result(6, 10, combinationOfThreeDice),
            new Result(7, 12, combinationOfThreeDice),
            new Result(8, 12, combinationOfThreeDice),
            new Result(9, 10, combinationOfThreeDice),
            new Result(10, 6, combinationOfThreeDice),
            new Result(11, 3, combinationOfThreeDice),
            new Result(12, 1, combinationOfThreeDice)
        );
    }

    private int numberOfCombinations(Dice dice) {

        return Double.valueOf(Math.pow(dice.sides, dice.dice)).intValue();
    }

    @Test
    void createsResultsOnlyInRange() {

        Stream.of(0, 1, 6, 17, 100).forEach(cardinality ->
            Stream.of(1, 2, 6, 17, 100).forEach(sides ->
                Stream.of(-23, 0, 1, 10, 100).forEach(modifier ->
                    assertResultsInRange(Dice.define()
                        .withDice(cardinality)
                        .withSides(sides)
                        .withMod(modifier)
                        .create()))
            )
        );
    }

    private void assertResultsInRange(Dice dice) {

        int expectedMinimum = dice.dice + dice.mod;
        int expectedMaximum = dice.dice * dice.sides + dice.mod;

        Set<Integer> results = IntStream.range(0, SAMPLE_SIZE)
            .map(i -> dice.throwDice())
            .distinct()
            .boxed()
            .collect(toSet());

        assertThat(results)
            .describedAs("%dd%d+%d", dice.dice, dice.sides, dice.mod)
            .allSatisfy(value -> assertThat(value)
                .isBetween(expectedMinimum, expectedMaximum));
    }

    @Test
    void calculatesResultBasedOnRandomSource() {

        RandomInt random = prepareRandomSource(4, 6, 1, 8, 0, 12);
        assertThat(performRoll(Dice.define(random)
            .withSides(13)
            .create(), 7))
            .containsExactly(5, 7, 2, 9, 1, 13, 13);
        verify(random, times(7)).next(13);

        random = prepareRandomSource(3, 2, 0, 3, 0, 2, 1, 4, 3);
        assertThat(performRoll(Dice.define(random)
            .withSides(5)
            .withDice(2)
            .create(), 5))
            .containsExactly(7, 5, 4, 7, 8);
        verify(random, times(10)).next(5);

        random = prepareRandomSource(4, 2, 6, 3, 8, 2, 9);
        assertThat(performRoll(Dice.define(random)
            .withSides(10)
            .withMod(2)
            .create(), 8))
            .containsExactly(7, 5, 9, 6, 11, 5, 12, 12);
        verify(random, times(8)).next(10);
    }

    private RandomInt prepareRandomSource(Integer first, Integer... values) {

        RandomInt random = Mockito.mock(RandomInt.class);
        when(random.next(anyInt())).thenReturn(first, values);
        return random;
    }

    private List<Integer> performRoll(Dice dice, int times) {

        return IntStream.range(0, times)
            .map(i -> dice.throwDice())
            .boxed()
            .toList();
    }

}
