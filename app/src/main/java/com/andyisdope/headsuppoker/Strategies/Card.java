package com.andyisdope.headsuppoker.Strategies;

import java.util.Objects;

/**
 * Created by Andy on 9/12/2017.
 */

public class Card {

    public final Integer Rank;
    public final String Suit;
    public final String Color;

    public Card(Integer rank, String suit) {
        Rank = rank;
        Suit = suit;
        if (suit.equals("Clubs") || suit.equals("Spades")) {
            Color = "Black";
        } else {
            Color = "Red";
        }
    }

    public Card(char rank, char suit) {
        switch (rank) {
            case 'a':
                Rank = 14;
                break;
            case 'q':
                Rank = 12;
                break;
            case 'j':
                Rank = 11;
                break;
            case 'k':
                Rank = 13;
                break;
            case 't':
                Rank = 10;
                break;
            default:
                Rank = Integer.parseInt(String.valueOf(rank));
                break;
        }
        Suit = String.valueOf(suit);
        if (Suit.equals("Clubs") || Suit.equals("Spades")) {
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

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;

        Card chk = (Card) obj;
        return (Objects.equals(this.Rank, chk.Rank) && this.Suit.equals(chk.Suit));
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + Objects.hashCode(this.Rank);
        hash = 67 * hash + Objects.hashCode(this.Suit);
        return hash;
    }

}

