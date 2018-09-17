package com.xskr.onk_v1.core.exchange;

import com.xskr.onk_v1.core.Card;
//预言家查阅牌堆中的两张牌
public class SeerCheckDeck extends Operation{

    int seerSeat;
    int deck1;
    Card card1;
    int deck2;
    Card card2;

    public int getSeerSeat() {
        return seerSeat;
    }

    public void setSeerSeat(int seerSeat) {
        this.seerSeat = seerSeat;
    }

    public int getDeck1() {
        return deck1;
    }

    public void setDeck1(int deck1) {
        this.deck1 = deck1;
    }

    public Card getCard1() {
        return card1;
    }

    public void setCard1(Card card1) {
        this.card1 = card1;
    }

    public int getDeck2() {
        return deck2;
    }

    public void setDeck2(int deck2) {
        this.deck2 = deck2;
    }

    public Card getCard2() {
        return card2;
    }

    public void setCard2(Card card2) {
        this.card2 = card2;
    }
}
