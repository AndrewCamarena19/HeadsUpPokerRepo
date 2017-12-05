package com.andyisdope.headsuppoker;

import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;


/**
 * Created by Andy on 11/28/2017.
 */

public class PokerUtilities {

    private static double wins = 0;
    private static ArrayList<String> Suits = new ArrayList<>(Arrays.asList("c", "s", "h", "d"));
    private static ArrayList<Card> Deck = new ArrayList<>();

    static void SetActionLabel(String act, TextView Seat1Chips, TextView Seat2Chips, final TextView Seat2Name, final TextView Seat1Name, final Player Player) {
        if (Player.getSeat().equals("Seat1")) {
            Seat1Chips.setText(String.format("%.2f", Player.getStack()) + "");
            Seat1Name.setText(act);
            Seat1Name.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Seat1Name.setText(Player.getUsername());
                }
            }, 2000);
        } else {
            Seat2Chips.setText(String.format("%.2f", Player.getStack()) + "");
            Seat2Name.setText(act);
            Seat2Name.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Seat2Name.setText(Player.getUsername());
                }
            }, 2000);
        }
    }

    static void SetActionLabelVillian(String act, final TextView Seat2Name, final TextView Seat1Name, String Seat, final String Villian) {
        if (Seat.equals("Seat1")) {
            Seat1Name.setText(act);
            Seat1Name.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Seat1Name.setText(Villian);
                }
            }, 2000);
        } else {
            Seat2Name.setText(act);
            Seat2Name.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Seat2Name.setText(Villian);
                }
            }, 2000);
        }
    }

    public static int CheckWinner(PokerHand Seat1, PokerHand Seat2, TextView Pot) {
        Seat1.CalculateHand();
        Seat2.CalculateHand();
        if (Seat1.getHandStrength() > Seat2.getHandStrength()) {
            Pot.setText("Seat 1 Wins");
            return 1;
        } else if (Seat1.getHandStrength() < Seat2.getHandStrength()) {
            Pot.setText("Seat 2 Wins");
            return 2;
        } else {
            //easy check for first card
            if (Seat1.getCard(0).getRank() > Seat2.getCard(0).getRank()) {
                Pot.setText("Seat 1 Wins");
                return 1;
            } else if (Seat1.getCard(0).getRank() < Seat2.getCard(0).getRank()) {
                Pot.setText("Seat 2 Wins");
                return 2;
            } else {
                //may need to check all cards
                int count = 1;
                boolean done = false;
                while (count < 5 && !done) {
                    if (Seat1.getCard(count).getRank() > Seat2.getCard(count).getRank()) {
                        Pot.setText("Seat 1 Wins");
                        return 1;
                    } else if (Seat1.getCard(count).getRank() < Seat2.getCard(count).getRank()) {
                        Pot.setText("Seat 2 Wins");
                        return 2;
                    } else
                        count++;
                }
                if (!done) {
                    Pot.setText("Split Pot");
                    return 0;
                }
            }
        }
        return -1;
    }

    private static void CheckEquity(PokerHand Seat1, PokerHand Seat2) {
        Seat1.CalculateHand();
        Seat2.CalculateHand();
        if (Seat1.getHandStrength() > Seat2.getHandStrength()) {
            wins++;
        } else if (Seat1.getHandStrength() < Seat2.getHandStrength()) {

        } else {
            //easy check for first card
            if (Seat1.getCard(0).getRank() > Seat2.getCard(0).getRank()) {
                wins++;
            } else if (Seat1.getCard(0).getRank() < Seat2.getCard(0).getRank()) {
            } else {
                //may need to check all cards
                int count = 1;
                boolean done = false;
                while (count < 5 && !done) {
                    if (Seat1.getCard(count).getRank() > Seat2.getCard(count).getRank()) {
                        wins++;
                        done = true;
                    } else if (Seat1.getCard(count).getRank() < Seat2.getCard(count).getRank()) {
                        //Pot.setText("Seat 2 wins");
                        done = true;
                    } else
                        count++;
                }
                if (!done) {
                }
            }
        }
    }

    public static Double Equity(PokerHand Seat1, PokerHand Seat2) {
        HashSet CardsHand = new HashSet();
        CardsHand.addAll(Seat1.getHand());
        CardsHand.addAll(Seat2.getHand());

        for (int i = 0; i < Suits.size(); i++) {
            for (int k = 1; k < 14; k++) {
                Card toAdd = new Card(k, Suits.get(i));
                if (!CardsHand.contains(toAdd))
                    Deck.add(toAdd);
            }
        }
        double totalhands = 0;
        Collections.shuffle(Deck);
        for (int i = 0; i < Deck.size(); i++)
            for (int k = i + 1; k < Deck.size(); k++)
                for (int j = k + 1; j < Deck.size(); j++)
                    for (int l = j + 1; l < Deck.size(); l++)
                        for (int m = l + 1; m < Deck.size(); m++) {
                            PokerHand me2 = new PokerHand();
                            for (Card c : Seat1.getHand())
                                me2.addCard(c);
                            me2.addCard(Deck.get(i));
                            me2.addCard(Deck.get(j));
                            me2.addCard(Deck.get(k));
                            me2.addCard(Deck.get(l));
                            me2.addCard(Deck.get(m));
                            PokerHand opp2 = new PokerHand();
                            for (Card c : Seat2.getHand())
                                opp2.addCard(c);
                            opp2.addCard(Deck.get(i));
                            opp2.addCard(Deck.get(j));
                            opp2.addCard(Deck.get(k));
                            opp2.addCard(Deck.get(l));
                            opp2.addCard(Deck.get(m));
                            CheckEquity(me2, opp2);
                            totalhands++;
                        }

        double percent = wins / totalhands;
        return (percent * 100);
    }
}

