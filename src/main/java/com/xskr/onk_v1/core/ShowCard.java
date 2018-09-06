package com.xskr.onk_v1.core;

public class ShowCard {

    private ShowCardType type;
    private int number;
    private Card card;

    public ShowCard(ShowCardType type, int number, Card card){
        this.type = type;
        this.number = number;
        this.card = card;
    }

    public ShowCardType getType() {
        return type;
    }

    public void setType(ShowCardType type) {
        this.type = type;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public Card getCard() {
        return card;
    }

    public void setCard(Card card) {
        this.card = card;
    }
}


