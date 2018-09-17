package com.xskr.onk_v1.core;

import java.util.ArrayList;
import java.util.List;
public class Summary {
    Camp camp;
    int seat;
    boolean win;
    Card initializeCard;
    Card finalCard;
    List operations = new ArrayList();

    public Summary(Camp camp, int seat, boolean win, Card initializeCard, Card finalCard, List operations) {
        this.camp = camp;
        this.seat = seat;
        this.win = win;
        this.initializeCard = initializeCard;
        this.finalCard = finalCard;
        this.operations = operations;
    }

    public Camp getCamp() {
        return camp;
    }

    public void setCamp(Camp camp) {
        this.camp = camp;
    }

    public int getSeat() {
        return seat;
    }

    public void setSeat(int seat) {
        this.seat = seat;
    }

    public boolean isWin() {
        return win;
    }

    public void setWin(boolean win) {
        this.win = win;
    }

    public Card getInitializeCard() {
        return initializeCard;
    }

    public void setInitializeCard(Card initializeCard) {
        this.initializeCard = initializeCard;
    }

    public Card getFinalCard() {
        return finalCard;
    }

    public void setFinalCard(Card finalCard) {
        this.finalCard = finalCard;
    }

    public List getOperations() {
        return operations;
    }

    public void setOperations(List operations) {
        this.operations = operations;
    }
}
