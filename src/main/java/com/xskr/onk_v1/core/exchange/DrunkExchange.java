package com.xskr.onk_v1.core.exchange;

import com.xskr.onk_v1.core.Card;

public class DrunkExchange extends Operation{
    //牌垛编号
    int deckNumber;
    //交换前牌垛牌
    Card deckCard;
    public int getDeckNumber() {
        return deckNumber;
    }

    public void setDeckNumber(int deckNumber) {
        this.deckNumber = deckNumber;
    }

    public Card getDeckCard() {
        return deckCard;
    }

    public void setDeckCard(Card deckCard) {
        this.deckCard = deckCard;
    }
}
