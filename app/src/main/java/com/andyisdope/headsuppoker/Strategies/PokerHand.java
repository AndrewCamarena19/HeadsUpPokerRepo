package com.andyisdope.headsuppoker.Strategies;

public class PokerHand extends Hand {

    private String FlushSuit;
    private int HandStrength;
    private String HandName;
    private boolean Flush = false;
    private boolean Straight = false;
    private int clubs;
    private int diamonds;
    private int hearts;
    private int spades;
    private PokerRankStrat RankStrat;

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

    public boolean hasFlush() {
        return Flush;
    }

    public String getFlushSuit() {
        return FlushSuit;
    }

    public boolean canStraight() {
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

    public String getHandName() {
        return HandName;
    }

    public void setHandName(String toSet) {
        HandName = toSet;
    }

    public int getHandStrength() {
        return HandStrength;
    }

    public void setHandStrength(int handStrength) {
        HandStrength = handStrength;
    }

    public void setRankStrat(PokerRankStrat rs) {
        RankStrat = rs;
    }

    public int RankHands(PokerHand villian)
    {
        return RankStrat.compareHands(this, villian);
    }
}