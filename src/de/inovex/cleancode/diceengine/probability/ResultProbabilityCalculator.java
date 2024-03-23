package de.inovex.cleancode.diceengine.probability;

import static java.util.Collections.emptyList;
import static java.util.Comparator.comparing;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Predicate;

import de.inovex.cleancode.diceengine.Dice;
import de.inovex.cleancode.diceengine.Result;

public class ResultProbabilityCalculator {

    public double probability(ResultCategory cat, Dice... dice) {

        final int v = cat.value();
        final Predicate<Integer> filter = switch (cat.operator()) {
            case EQUAL -> r -> r == v;
            case LESS -> r -> r < v;
            case LESS_OR_EQUAL -> r -> r <= v;
            case GREATER -> r -> r > v;
            case GREATER_OR_EQUAL -> r -> r >= v;
        };

        double probability = 0.0;
        if (dice.length == 0) {
            return probability;
        }

        List<Result> distribution = emptyList();
        if (dice.length == 1) {
            distribution = dice[0].resultDistribution();
        } else {

            // collect all possible sums of all rolls
          List<Result> allResultSums = new ArrayList<>();
            int[] indices = new int[dice.length];
            Arrays.fill(indices, 0);
            List<Result[]> diceDistributions = Arrays.stream(dice)
                .map(Dice::resultDistribution)
                .map(list -> list.toArray(new Result[0]))
                .toList();
            boolean overflow = false;
            while (!overflow) {
                int x = 0;
                int r = 0;
                int c = 1;
                int o = 1;
                for(Result[] results: diceDistributions) {
                    int index = indices[x++];
                    Result current = results[index];
                    r += current.result();
                    c *= current.count();
                    o *= current.overall();
                }
                allResultSums.add(new Result(r, c, o));

                overflow = true;
                for(int i = dice.length - 1; i >= 0 && overflow; i--) {
                    if (indices[i] == diceDistributions.get(i).length - 1) {
                        indices[i] = 0;
                    } else {
                        indices[i]++;
                        overflow = false;
                    }
                }
            }

            // sum up items with same result value
            distribution = new ArrayList<>();
            Collections.sort(allResultSums, comparing(Result::result));
            Result current = null;
            for(Result result: allResultSums) {
                if (current == null) {
                    current = result;
                } else {
                    if (current.result() == result.result()) {
                        current = new Result(current.result(), current.count() + result.count(), current.overall());
                    } else {
                        distribution.add(current);
                        current = result;
                    }
                }
            }
            if (current != null) {
                distribution.add(current);
            }
        }

        for (Result result : distribution) {
            if (filter.test(result.result())) {
                probability += result.count() / (double) result.overall();
            }
        }

        return probability;
    }

}
