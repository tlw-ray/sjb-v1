package com.xskr.onk_v1.core.operation;

//捣蛋鬼换牌
public class TroublemakerExchange extends Operation {

    int fromSeat;
    int toSeat;

    public int getFromSeat() {
        return fromSeat;
    }

    public void setFromSeat(int fromSeat) {
        this.fromSeat = fromSeat;
    }

    public int getToSeat() {
        return toSeat;
    }

    public void setToSeat(int toSeat) {
        this.toSeat = toSeat;
    }
}
