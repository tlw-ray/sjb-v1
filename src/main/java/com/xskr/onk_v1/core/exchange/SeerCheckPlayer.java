package com.xskr.onk_v1.core.exchange;

public class SeerCheckPlayer extends Operation{

    int seerSeat;
    int seerCard;

    int checkedSeat;
    int checkedCard;

    public int getSeerSeat() {
        return seerSeat;
    }

    public void setSeerSeat(int seerSeat) {
        this.seerSeat = seerSeat;
    }

    public int getSeerCard() {
        return seerCard;
    }

    public void setSeerCard(int seerCard) {
        this.seerCard = seerCard;
    }

    public int getCheckedSeat() {
        return checkedSeat;
    }

    public void setCheckedSeat(int checkedSeat) {
        this.checkedSeat = checkedSeat;
    }

    public int getCheckedCard() {
        return checkedCard;
    }

    public void setCheckedCard(int checkedCard) {
        this.checkedCard = checkedCard;
    }
}
