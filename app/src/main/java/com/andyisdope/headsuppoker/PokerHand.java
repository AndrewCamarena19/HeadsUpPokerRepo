package com.andyisdope.headsuppoker;

/*
  Created by Andy on 9/12/2017.
 */

import java.util.ArrayList;

/**
 *
 * @author Andy
 */
@SuppressWarnings("ALL")
class PokerHand extends Hand {

    private String FlushSuit;
    private int HandStrength;
    private String HandName;
    private boolean Flush = false;
    private boolean Straight = false;
    private int clubs;
    private int diamonds;
    private int hearts;
    private int spades;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Card x : super.getHand()) {
            sb.append(" ").append(x.toString());
        }
        return sb.toString();
    }

    public PokerHand() {
        super();
        FlushSuit = "";
        HandName = "";
        HandStrength = 0;
        clubs = 0;
        diamonds = 0;
        hearts = 0;
        spades = 0;
    }

    private boolean Flush() {
        return Flush;
    }

    private String getFlushSuit() {
        return FlushSuit;
    }

    private boolean canStraight() {
        return Straight;
    }

    @Override
    public void addCard(Card card) {
        super.addCard(card);
        Card temp = null;
        if (card.getRank() == 1) {
            temp = (new Card(14, card.getSuit()));
        }
        if (card.getRank() == 14) {
            temp = (new Card(1, card.getSuit()));
        }
        if (card.getRank() == 5 || card.getRank() == 10) {
            Straight = true;
        }
        if (temp != null) {
            super.addCard(temp);
        }

        switch (card.getSuit()) {
            case "C":
                clubs++;
                break;
            case "S":
                spades++;
                break;
            case "D":
                diamonds++;
                break;
            case "H":
                hearts++;
                break;
        }
        if (super.HandSize() > 4) {
            if (clubs > 4) {
                FlushSuit = "C";
                Flush = true;
            } else if (spades > 4) {
                FlushSuit = "S";
                Flush = true;
            } else if (hearts > 4) {
                FlushSuit = "H";
                Flush = true;
            } else if (diamonds > 4) {
                FlushSuit = "D";
                Flush = true;
            } else {
                Flush = false;
            }
            //CalculateHand();
        }
    }

    public int getHandStrength() {
        return HandStrength;
    }

    public void CalculateHand() {
        int ranks[] = new int[15];
        for (Card card : super.getHand()) {
            ranks[card.getRank()]++;
        }
        ranks[1] = ranks[14];
        HandStrength = CheckHands(ranks);
    }

    private int CheckHands(int[] ranks) {
        if (hasStraightFlush(ranks)) {
            HandName = "Straight Flush";
            return 8;
        } else if (hasQuads(ranks)) {
            HandName = "Quads";
            return 7;
        } else if (hasFullHouse(ranks)) {
            HandName = "Full House";
            return 6;
        } else if (hasFlush()) {
            HandName = "Flush";
            return 5;
        } else if (hasStraight(ranks)) {
            HandName = "Straight";
            return 4;
        } else if (hasSet(ranks)) {
            HandName = "Set";
            return 3;
        } else if (hasPairs(ranks)) {
            HandName = "Two Pair";
            return 2;
        } else if (hasPair(ranks)) {
            HandName = "One Pair";
            return 1;
        } else {
            HandName = "High Card";
            HighCard();
            return 0;
        }
    }

    private boolean hasStraightFlush(int[] rank) {
        Hand toRemove = new Hand();
        if (!Flush() || !canStraight()) {
            return false;
        } else {
            for (Card x : super.getHand()) {
                if (!x.getSuit().equals(getFlushSuit())) {
                    toRemove.addCard(x);
                    rank[x.getRank()]--;
                }
            }
            for (Card x : toRemove.getHand()) {
                super.getHand().remove(x);
            }
            return hasStraight(rank);
        }
    }

    private boolean hasQuads(int[] ranks) {
        Hand toRemove = new Hand();
        int quad = 0;
        for (int i = 0; i < ranks.length; i++) {
            if (ranks[i] == 4) {
                quad = i;
            }
        }
        if (quad != 0) {
            for (Card x : super.getHand()) {
                if (x.getRank() != quad) {
                    toRemove.addCard(x);
                }
            }
            Card High = toRemove.getCard(0);
            for (Card x : toRemove.getHand()) {
                super.getHand().remove(x);
            }
            super.addCard(High);
            return true;
        }

        return false;
    }

    private boolean hasFullHouse(int[] ranks) {
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
            for (Card x : super.getHand()) {
                if (!x.getRank().equals(set) && !x.getRank().equals(pair)) {
                    toRemove.addCard(x);
                }
            }
            for (Card x : toRemove.getHand()) {
                super.getHand().remove(x);
            }
            while (super.HandSize() > 5) {
                super.getHand().remove(super.HandSize() - 1);
            }
            return true;
        }
        return false;
    }

    private boolean hasFlush() {
        if (Flush) {
            Hand Flushs = new Hand();
            for (Card x : super.getHand()) {
                if (x.getSuit().equals(FlushSuit)) {
                    Flushs.addCard(x);
                }
            }
            if (Flushs.HandSize() > 5) {
                int size = Flushs.HandSize();
                for (int i = size - 1; i > 4; i--) {
                    Flushs.getHand().remove(i);
                }
            }
            super.SetHand(Flushs);
            return true;
        }
        return false;

    }

    private boolean hasStraight(int[] ranks) {
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
            for (Card x : super.getHand()) {
                if (!x.getRank().equals(start) && !x.getRank().equals(start - 1) && !x.getRank().equals(start - 2) && !x.getRank().equals(start - 3) && !x.getRank().equals(start - 4)) {
                    toRemove.addCard(x);
                }
            }

            for (Card x : toRemove.getHand()) {
                super.getHand().remove(x);
            }
            if (super.HandSize() > 5) {
                for (int i = 0; i < super.HandSize() - 1; i++) {
                    if (super.getCard(i).getRank().equals(super.getCard(i + 1).getRank())) {
                        super.getHand().remove(i);
                    }
                    if (super.HandSize() == 5) {
                        break;
                    }
                }

            }
        }
        return hasit;
    }

    private boolean hasSet(int[] ranks) {
        Hand toRemove = new Hand();
        int set = 0;
        for (int i = 0; i < ranks.length; i++) {
            if (ranks[i] == 3) {
                set = i;
            }
        }
        if (set > 0) {
            for (Card x : super.getHand()) {
                if (!x.getRank().equals(set)) {
                    toRemove.addCard(x);
                }
            }

            Card High1 = toRemove.getCard(0);
            Card High2 = toRemove.getCard(1);

            for (Card x : toRemove.getHand()) {
                super.getHand().remove(x);
            }

            super.addCard(High1);
            super.addCard(High2);
            return true;
        }
        return false;
    }

    private boolean hasPairs(int[] ranks) {
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
            for (Card x : super.getHand()) {
                if (!x.getRank().equals(pairs.get(0)) && !x.getRank().equals(pairs.get(1))) {
                    toRemove.addCard(x);
                }
            }
            Card High = toRemove.getCard(0);

            for (Card x : toRemove.getHand()) {
                super.getHand().remove(x);
            }

            super.addCard(High);
            return true;
        }
        return false;
    }

    private boolean hasPair(int[] ranks) {
        Hand toRemove = new Hand();
        int pair = 0;
        for (int i = 0; i < ranks.length; i++) {
            if (ranks[i] == 2) {
                pair = i;
            }
        }
        if (pair != 0) {
            for (Card x : super.getHand()) {
                if (x.getRank() != pair) {
                    toRemove.addCard(x);
                }
            }
            Card High1 = toRemove.getCard(0);
            Card High2 = toRemove.getCard(1);
            Card High3 = toRemove.getCard(2);

            for (Card x : toRemove.getHand()) {
                super.getHand().remove(x);
            }
            super.addCard(High1);
            super.addCard(High2);
            super.addCard(High3);

            return true;
        }
        return false;
    }

    private void HighCard() {
        while (super.HandSize() > 5) {
            super.getHand().remove(super.HandSize() - 1);
        }

    }

    public String getHandName()
    {
        return HandName;
    }

}