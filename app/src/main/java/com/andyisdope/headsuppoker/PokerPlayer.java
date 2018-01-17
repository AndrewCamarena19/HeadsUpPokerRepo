package com.andyisdope.headsuppoker;

/**
 * Created by Andy on 1/17/2018.
 */

public class PokerPlayer {

    private static final PokerPlayer ourInstance = new PokerPlayer();

    public static PokerPlayer getInstance() {
        return ourInstance;
    }

    private String Seat;
    private String Username;
    private Double Bankroll;
    private Double Stack;
    private String UID;
    private Boolean Dealer;

    public boolean getDealer() {
        return Dealer;
    }

    public void setDealer(Boolean dealer) {
        Dealer = dealer;
    }

    public String getUID() {
        return UID;
    }

    private PokerPlayer(){}

    public void addtoBank(Double toAdd) {
        Bankroll += toAdd;
    }

    public void removefromBank(Double toTake) {
        if (toTake > Bankroll)
            Bankroll = 0.0;
        else
            Bankroll -= toTake;
    }

    public String getSeat() {
        return Seat;
    }

    public void setSeat(String seat) {
        Seat = seat;
    }

    public String getUsername() {
        return Username;
    }

    public void setUsername(String username) {
        Username = username;
    }

    public Double getBankroll() {
        return Bankroll;
    }

    public void setBankroll(Double bankroll) {
        Bankroll = bankroll;
    }

    public Double getStack() {
        return Stack;
    }

    public void setStack(Double stack) {
        if (Bankroll >= stack) {
            Stack = stack;
            Bankroll -= stack;
        }
    }

    public void addToStack(Double stack) {
        Stack += stack;
    }

    public void removeFromStack(Double stack) {
        Stack -= stack;
    }

    public void setUID(String UID) {
        this.UID = UID;
    }
}
