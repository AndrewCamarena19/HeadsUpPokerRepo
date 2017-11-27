package com.andyisdope.headsuppoker;

/**
 * Created by Andy on 9/12/2017.
 */

public class Card {

    private final Integer Rank;
    private final String Suit;
    private final String Color;

    public Card(Integer rank, String suit) {
        Rank = rank;
        Suit = suit;
        if (suit.equals("Clubs") || suit.equals("Spades")) {
            Color = "Black";
        } else {
            Color = "Red";
        }
    }

    public Integer getRank() {
        return Rank;
    }

    public String getSuit() {
        return Suit;
    }

    @Override
    public String toString() {
        switch (Rank) {
            case 1:
            case 14:
                return Suit + "a";
            case 11:
                return Suit + "j";
            case 12:
                return Suit + "q";
            case 13:
                return Suit + "k";
            case 10:
                return Suit + "t";
        }
        return Suit + Rank;
    }

    public boolean isCard(Card check) {
        return (this.Rank == check.Rank && this.Suit.equals(check.Suit));
    }

}

