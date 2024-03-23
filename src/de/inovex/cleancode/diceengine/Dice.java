package de.inovex.cleancode.diceengine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

import org.assertj.core.util.VisibleForTesting;

public class Dice {

    private final static Random RANDOM = new Random();

    private final static int MIN = 1;


    final int sides;

    final int dice;

    final int mod;

    private final RandomInt rnd;

    public Dice(int sides, int dice, int mod, RandomInt rnd) {

        this.sides = sides;
        this.dice = dice;
        this.mod = mod;
        this.rnd = rnd;
    }

    public int minimum() {

        return dice * MIN + mod;
    }

    public int maximum() {

        return dice * sides + mod;
    }

    public List<Result> resultDistribution() {

        if (dice == 0) {
            return List.of(new Result(0, 1, 1));

        } else {
            List<Result> list = new ArrayList<>();
            Iterable<Integer> iterable = DiceValues::new;
            List<Integer> all = StreamSupport.stream(iterable.spliterator(), false).sorted().toList();
            int prev = 0;
            int c = 0;
            for (int currentValue: all) {
                if (currentValue == prev) {
                    c++;
                } else {
                    if (c > 0) {
                        list.add(new Result(prev + mod, c, all.size()));
                    }
                    prev = currentValue;
                    c = 1;
                }
            }
            list.add(new Result(prev + mod, c, all.size()));

            return list;
        }
    }

    public int throwDice() {

        if (dice == 0) {
            return mod;
        }
        return IntStream.range(0, dice)
            .map(d -> rnd.next(sides) + MIN) // random returns zero inclusive, we need a minimum of one
            .sum() + mod;
    }


    private class DiceValues implements Iterator<Integer> {

        private final int[] values = new int[dice];

        private boolean hasNext = true;

        public DiceValues() {

            Arrays.fill(values, 1);
        }

        @Override
        public boolean hasNext() {

            return hasNext;
        }

        @Override
        public Integer next() {

            int currentValue = Arrays.stream(values).sum();
            hasNext = !increment(0);
            return currentValue;
        }

        private boolean increment(int index) {

            if ((index == dice - 1) || increment(index + 1)) {
                int currentValue = values[index];
                if (currentValue < sides) {
                    values[index] = currentValue + 1;
                } else {
                    // overflow
                    values[index] = 1;
                    return true;
                }
            }
            return false;
        }
    }


    public static DiceDefinition define() {

        return new DiceDefinition(RANDOM::nextInt);
    }

    @VisibleForTesting
    protected static DiceDefinition define(RandomInt random) {

        return new DiceDefinition(random);
    }

    public static class DiceDefinition {

        private Integer sides;
        private int dice = 1;
        private int mod = 0;
        private final RandomInt rnd;

        private DiceDefinition(RandomInt rnd) {

            this.rnd = rnd;
        }

        public DiceDefinition withSides(int sides) {

            this.sides = sides;
            return this;
        }

        public DiceDefinition withDice(int dice) {

            this.dice = dice;
            return this;
        }

        public DiceDefinition withMod(int mod) {

            this.mod = mod;
            return this;
        }

        public Dice create() {

            if (sides == null) {
                throw new IllegalArgumentException("Number of sides must be defined.");
            }
            if (sides < MIN) {
                throw new IllegalArgumentException("Number of sides must be positive. Invalid result: " + sides);
            }
            if (dice < 0) {
                throw new IllegalArgumentException("Cardinality of dice cannot be negative. Invalid result: " + dice);
            }

            long maxValue = ((long) dice) * ((long) sides) + ((long) mod);
            if (maxValue > Integer.MAX_VALUE) {
                throw new IllegalArgumentException("The maximal possible value overflows the valid range.");
            }
            return new Dice(sides, dice, mod, rnd);
        }
    }
}
