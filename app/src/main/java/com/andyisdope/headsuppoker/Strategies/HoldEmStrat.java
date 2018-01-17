package com.andyisdope.headsuppoker.Strategies;

import java.util.ArrayList;

/**
 * Created by Andy on 1/17/2018.
 */

public class HoldEmStrat implements PokerRankStrat {

    @Override
    public void calculateHand(PokerHand hand) {
        CalculateHand(hand);
    }

    @Override
    public int compareHands(PokerHand Seat1, PokerHand Seat2) {
            calculateHand(Seat1);
            calculateHand(Seat2);
            if (Seat1.getHandStrength() > Seat2.getHandStrength()) {
                return 1;
            } else if (Seat1.getHandStrength() < Seat2.getHandStrength()) {
                return 2;
            } else {
                //easy check for first card
                if (Seat1.getCard(0).getRank() > Seat2.getCard(0).getRank()) {
                    return 1;
                } else if (Seat1.getCard(0).getRank() < Seat2.getCard(0).getRank()) {
                    return 2;
                } else {
                    //may need to check all cards
                    int count = 1;
                    boolean done = false;
                    while (count < 5 && !done) {
                        if (Seat1.getCard(count).getRank() > Seat2.getCard(count).getRank()) {
                            done = true;
                            return 1;
                        } else if (Seat1.getCard(count).getRank() < Seat2.getCard(count).getRank()) {
                            done = true;
                            return 2;
                        } else
                            count++;
                    }
                    if (!done) {
                        return 0;
                    }
                }
            }
            return -1;
    }

    private void CalculateHand(PokerHand hand) {
        int ranks[] = new int[15];
        for (Card card : hand.getHand()) {
            ranks[card.getRank()]++;
        }
        ranks[1] = ranks[14];
        hand.setHandStrength(CheckHands(ranks,hand));
    }

    private int CheckHands(int[] ranks, PokerHand hand) {
        if (hasStraightFlush(ranks, hand)) {
            hand.setHandName("Straight Flush");
            return 8;
        } else if (hasQuads(ranks, hand)) {
            hand.setHandName("Quads");
            return 7;
        } else if (hasFullHouse(ranks, hand)) {
            hand.setHandName("Full House");
            return 6;
        } else if (hasFlush(hand)) {
            hand.setHandName("Flush");
            return 5;
        } else if (hasStraight(ranks, hand)) {
            hand.setHandName("Straight");
            return 4;
        } else if (hasSet(ranks, hand)) {
            hand.setHandName("Set");
            return 3;
        } else if (hasPairs(ranks, hand)) {
            hand.setHandName("Two Pair");
            return 2;
        } else if (hasPair(ranks, hand)) {
            hand.setHandName("Pair");
            return 1;
        } else {
            hand.setHandName("High Card");
            HighCard(hand);
            return 0;
        }
    }

    private boolean hasStraightFlush(int[] rank, PokerHand hand) {
        Hand toRemove = new Hand();
        if (!hand.hasFlush() || !hand.canStraight()) {
            return false;
        } else {
            for (Card x : hand.getHand()) {
                if (!x.getSuit().equals(hand.getFlushSuit())) {
                    toRemove.addCard(x);
                    rank[x.getRank()]--;
                }
            }
            for (Card x : toRemove.getHand()) {
                hand.getHand().remove(x);
            }
            return hasStraight(rank, hand);
        }
    }

    private boolean hasQuads(int[] ranks, PokerHand hand) {
        Hand toRemove = new Hand();
        int quad = 0;
        for (int i = 0; i < ranks.length; i++) {
            if (ranks[i] == 4) {
                quad = i;
            }
        }
        if (quad != 0) {
            for (Card x : hand.getHand()) {
                if (x.getRank() != quad) {
                    toRemove.addCard(x);
                }
            }
            Card High = toRemove.getCard(0);
            for (Card x : toRemove.getHand()) {
                hand.getHand().remove(x);
            }
            hand.addCard(High);
            return true;
        }

        return false;
    }

    private boolean hasFullHouse(int[] ranks, PokerHand hand) {
        Hand toRemove = new Hand();
        int set = 0;
        int pair = 0;
        for (int i = 0; i < ranks.length; i++) {
            if (ranks[i] >= 2 && i != set) {
                pair = i;
            }
            if (ranks[i] == 3) {
                pair = set;
                set = i;
            }
        }
        if (set > 1 && pair > 1) {
            for (Card x : hand.getHand()) {
                if (!x.getRank().equals(set) && !x.getRank().equals(pair)) {
                    toRemove.addCard(x);
                }
            }
            for (Card x : toRemove.getHand()) {
                hand.getHand().remove(x);
            }
            while (hand.HandSize() > 5) {
                hand.getHand().remove(hand.HandSize() - 1);
            }
            return true;
        }
        return false;
    }

    private boolean hasFlush(PokerHand hand) {
        if (hand.hasFlush()) {
            Hand Flushs = new Hand();
            for (Card x : hand.getHand()) {
                if (x.getSuit().equals(hand.getFlushSuit())) {
                    Flushs.addCard(x);
                }
            }
            if (Flushs.HandSize() > 5) {
                int size = Flushs.HandSize();
                for (int i = size - 1; i > 4; i--) {
                    Flushs.getHand().remove(i);
                }
            }
            hand.SetHand(Flushs);
            return true;
        }
        return false;

    }

    private boolean hasStraight(int[] ranks, PokerHand hand) {
        boolean hasit = false;
        int start = 0;
        Hand toRemove = new Hand();
        for (int i = 14; i >= 5; i--) {
            if (ranks[i] > 0) {
                if (ranks[i - 1] > 0 && ranks[i - 2] > 0 && ranks[i - 3] > 0 && ranks[i - 4] > 0) {
                    hasit = true;
                    start = i;
                    break;
                }
            }
        }
        if (start > 0) {
            for (Card x : hand.getHand()) {
                if (!x.getRank().equals(start) && !x.getRank().equals(start - 1) && !x.getRank().equals(start - 2) && !x.getRank().equals(start - 3) && !x.getRank().equals(start - 4)) {
                    toRemove.addCard(x);
                }
            }

            for (Card x : toRemove.getHand()) {
                hand.getHand().remove(x);
            }
            if (hand.HandSize() > 5) {
                for (int i = 0; i < hand.HandSize() - 1; i++) {
                    if (hand.getCard(i).getRank().equals(hand.getCard(i + 1).getRank())) {
                        hand.getHand().remove(i);
                    }
                    if (hand.HandSize() == 5) {
                        break;
                    }
                }

            }
        }
        return hasit;
    }

    private boolean hasSet(int[] ranks, PokerHand hand) {
        Hand toRemove = new Hand();
        int set = 0;
        for (int i = 0; i < ranks.length; i++) {
            if (ranks[i] == 3) {
                set = i;
            }
        }
        if (set > 0) {
            for (Card x : hand.getHand()) {
                if (!x.getRank().equals(set)) {
                    toRemove.addCard(x);
                }
            }

            Card High1 = toRemove.getCard(0);
            Card High2 = toRemove.getCard(1);

            for (Card x : toRemove.getHand()) {
                hand.getHand().remove(x);
            }

            hand.addCard(High1);
            hand.addCard(High2);
            return true;
        }
        return false;
    }

    private boolean hasPairs(int[] ranks, PokerHand hand) {
        ArrayList<Integer> pairs = new ArrayList<>();
        Hand toRemove = new Hand();
        for (int i = 1; i < ranks.length; i++) {
            if (ranks[i] == 2) {
                pairs.add(i);
            }
        }
        if (pairs.size() > 0 && pairs.get(0) == 1) {
            pairs.remove(0);
        }

        if (pairs.size() > 1) {
            if (pairs.size() > 2) {
                pairs.remove(0);
            }
            for (Card x : hand.getHand()) {
                if (!x.getRank().equals(pairs.get(0)) && !x.getRank().equals(pairs.get(1))) {
                    toRemove.addCard(x);
                }
            }
            Card High = toRemove.getCard(0);

            for (Card x : toRemove.getHand()) {
                hand.getHand().remove(x);
            }

            hand.addCard(High);
            return true;
        }
        return false;
    }

    private boolean hasPair(int[] ranks, PokerHand hand) {
        Hand toRemove = new Hand();
        int pair = 0;
        for (int i = 0; i < ranks.length; i++) {
            if (ranks[i] == 2) {
                pair = i;
            }
        }
        if (pair != 0) {
            for (Card x : hand.getHand()) {
                if (x.getRank() != pair) {
                    toRemove.addCard(x);
                }
            }
            Card High1 = toRemove.getCard(0);
            Card High2 = toRemove.getCard(1);
            Card High3 = toRemove.getCard(2);

            for (Card x : toRemove.getHand()) {
                hand.getHand().remove(x);
            }
            hand.addCard(High1);
            hand.addCard(High2);
            hand.addCard(High3);

            return true;
        }
        return false;
    }

    private void HighCard(PokerHand hand) {
        while (hand.HandSize() > 5) {
            hand.getHand().remove(hand.HandSize() - 1);
        }

    }
}
