package com.andyisdope.headsuppoker.Utilities;

import android.widget.TextView;

import com.andyisdope.headsuppoker.PokerPlayer;
import com.andyisdope.headsuppoker.Strategies.Card;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;


/**
 * Created by Andy on 11/28/2017.
 */

public class PokerUtilities {

    private static double wins = 0;
    private static final ArrayList<String> Suits = new ArrayList<>(Arrays.asList("c", "s", "h", "d"));
    private static final ArrayList<Card> Deck = new ArrayList<>();

    public static void SetActionLabel(String act, TextView Seat1Chips, TextView Seat2Chips, final TextView Seat2Name, final TextView Seat1Name, final PokerPlayer Player) {
        if (Player.getSeat().equals("Seat1")) {
            Seat1Chips.setText(String.format(Locale.ENGLISH, "%.2f ", Player.getStack()));
            Seat1Name.setText(act);
            Seat1Name.postDelayed(() -> Seat1Name.setText(Player.getUsername()), 2000);
        } else {
            Seat2Chips.setText(String.format(Locale.ENGLISH, "%.2f ", Player.getStack()));
            Seat2Name.setText(act);
            Seat2Name.postDelayed(() -> Seat2Name.setText(Player.getUsername()), 2000);
        }
    }

    public static void SetActionLabelVillian(String act, final TextView Seat2Name, final TextView Seat1Name, String Seat, final String V1, final String V2) {
        if (Seat.equals("Seat1")) {
            Seat1Name.setText(act);
            Seat1Name.postDelayed(() -> Seat1Name.setText(V1), 2000);
        } else {
            Seat2Name.setText(act);
            Seat2Name.postDelayed(() -> Seat2Name.setText(V2), 2000);
        }
    }

}

