package com.andyisdope.headsuppoker.Strategies;

/**
 * Created by Andy on 1/17/2018.
 */

public interface PokerRankStrat {
    void calculateHand(PokerHand hand);
    int compareHands(PokerHand Seat1, PokerHand Seat2);
}
