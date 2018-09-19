package com.xskr.onk_v1.core.exchange;

import com.xskr.onk_v1.core.Card;

//强盗换牌
public class RobberSnatch extends Operation{

    //被抢夺的人的座位
    int snatchedSeat;

    public int getSnatchedSeat() {
        return snatchedSeat;
    }

    public void setSnatchedSeat(int snatchedSeat) {
        this.snatchedSeat = snatchedSeat;
    }
}
