package com.andyisdope.headsuppoker;

/**
 * Created by Andy on 9/12/2017.
 */

class Player {

    private String Seat;
    private String Username;
    private Double Bankroll;
    private Double Stack;
    private PokerHand Hand;
    private String UID;
    private Boolean Dealer;

    Boolean getDealer() {
        return Dealer;
    }

    void setDealer(Boolean dealer) {
        Dealer = dealer;
    }

    String getUID() {
        return UID;
    }

    public Player(String U) {
        UID = U;
    }

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

    public Hand getHand() {
        return Hand;
    }

    public void setHand(PokerHand hand) {
        Hand = hand;
    }
}
