package com.andyisdope.headsuppoker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by Andy on 9/12/2017.
 */

@SuppressWarnings({"ALL", "DefaultFileTemplate"})
public class Hand {

    private ArrayList<Card> Cards;

    Hand() {
        Cards = new ArrayList<>();
    }

    public Hand(Card one, Card two)
    {
        Cards = new ArrayList<>();
        Cards.add(one);
        Cards.add(two);
    }

    void addCard(Card card) {
        Cards.add(card);
        Collections.sort(Cards, CardComparatorHigh);
    }

    public void addCardLow(Card card)
    {
        Cards.add(card);
        Collections.sort(Cards, CardComparatorLow);
    }

    public Card getCard(int index) {
        return Cards.get(index);
    }

    public ArrayList<Card> getHand() {
        return Cards;
    }

    int HandSize() {
        return Cards.size();
    }

    void SetHand(Hand nh) {
        Cards = new ArrayList<>();
        for (Card x : nh.getHand()) {
            addCard(x);
        }
    }

    private final Comparator<Card> CardComparatorHigh = new Comparator<Card>() {
        @Override
        public int compare(Card card, Card t1) {
            return t1.getRank().compareTo(card.getRank());
        }
    };

    private final Comparator<Card> CardComparatorLow = new Comparator<Card>() {
        @Override
        public int compare(Card card, Card t1) {
            return card.getRank().compareTo(t1.getRank());
        }
    };
}