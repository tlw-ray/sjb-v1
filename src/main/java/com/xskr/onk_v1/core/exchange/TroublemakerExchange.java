package com.xskr.onk_v1.core.exchange;

//捣蛋鬼换牌
public class TroublemakerExchange extends Operation {
    int fromSeat;
    int fromCard;

    int toSeat;
    int toCard;

    public int getFromSeat() {
        return fromSeat;
    }

    public void setFromSeat(int fromSeat) {
        this.fromSeat = fromSeat;
    }

    public int getFromCard() {
        return fromCard;
    }

    public void setFromCard(int fromCard) {
        this.fromCard = fromCard;
    }

    public int getToSeat() {
        return toSeat;
    }

    public void setToSeat(int toSeat) {
        this.toSeat = toSeat;
    }

    public int getToCard() {
        return toCard;
    }

    public void setToCard(int toCard) {
        this.toCard = toCard;
    }
}
