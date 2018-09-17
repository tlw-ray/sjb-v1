package com.xskr.onk_v1.core.exchange;

import com.xskr.onk_v1.core.Card;

public class WolfCheckDeck extends Operation{

    int wolfSeat;
    int deck;
    Card checkedCard;

    public int getWolfSeat() {
        return wolfSeat;
    }

    public void setWolfSeat(int wolfSeat) {
        this.wolfSeat = wolfSeat;
    }

    public int getDeck() {
        return deck;
    }

    public void setDeck(int deck) {
        this.deck = deck;
    }

    public Card getCheckedCard() {
        return checkedCard;
    }

    public void setCheckedCard(Card checkedCard) {
        this.checkedCard = checkedCard;
    }
}
