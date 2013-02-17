package com.bimbr.choreo.model;

/**
 * A dance move, as part of choreography.
 *
 * @author mmakowski
 */
public class Move {
    private final String symbol;

    public Move(final String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }
}
