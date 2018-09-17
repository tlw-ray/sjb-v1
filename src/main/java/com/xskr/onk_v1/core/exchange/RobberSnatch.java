package com.xskr.onk_v1.core.exchange;

import com.xskr.onk_v1.core.Card;

//强盗换牌
public class RobberSnatch extends Operation{
    //强盗座位
    int robberSeat;
    //强盗的牌
    Card robberCard;
    //被抢夺的人的座位
    int snatchedSeat;
    //被抢夺的人的牌
    Card snatchedCard;

    public int getRobberSeat() {
        return robberSeat;
    }

    public void setRobberSeat(int robberSeat) {
        this.robberSeat = robberSeat;
    }

    public Card getRobberCard() {
        return robberCard;
    }

    public void setRobberCard(Card robberCard) {
        this.robberCard = robberCard;
    }

    public int getSnatchedSeat() {
        return snatchedSeat;
    }

    public void setSnatchedSeat(int snatchedSeat) {
        this.snatchedSeat = snatchedSeat;
    }

    public Card getSnatchedCard() {
        return snatchedCard;
    }

    public void setSnatchedCard(Card snatchedCard) {
        this.snatchedCard = snatchedCard;
    }
}
