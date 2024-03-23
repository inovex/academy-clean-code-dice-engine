package de.inovex.cleancode.diceengine;

@FunctionalInterface
public interface RandomInt {

    /**
     * @param bound exclusive positive bound
     * @return a pseudo-randomly choosen int between zero (incl) and the bound (excl).
     */
    int next(int bound);
}
