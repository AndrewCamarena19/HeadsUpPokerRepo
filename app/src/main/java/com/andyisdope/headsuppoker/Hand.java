package com.andyisdope.headsuppoker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by Andy on 9/12/2017.
 */

public class Hand {

    private ArrayList<Card> Cards;

    public Hand() {
        Cards = new ArrayList();
    }

    public Hand(Card one, Card two)
    {
        Cards = new ArrayList();
        Cards.add(one);
        Cards.add(two);
    }

    public void addCard(Card card) {
        Cards.add(card);
        Collections.sort(Cards, CardComparator);
    }

    public Card getCard(int index) {
        return Cards.get(index);
    }

    public ArrayList<Card> getHand() {
        return Cards;
    }

    public int HandSize() {
        return Cards.size();
    }

    public void SetHand(Hand nh) {
        Cards = new ArrayList();
        for (Card x : nh.getHand()) {
            addCard(x);
        }
    }

    private Comparator<Card> CardComparator = new Comparator<Card>() {
        @Override
        public int compare(Card card, Card t1) {
            return t1.getRank().compareTo(card.getRank());
        }
    };
}