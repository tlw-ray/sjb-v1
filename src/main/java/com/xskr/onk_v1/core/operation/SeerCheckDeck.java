package com.xskr.onk_v1.core.exchange;

import com.xskr.onk_v1.core.Card;
//预言家查阅牌堆中的两张牌
public class SeerCheckDeck extends Operation{

    int deck1;
    int deck2;

    public int getDeck1() {
        return deck1;
    }

    public void setDeck1(int deck1) {
        this.deck1 = deck1;
    }

    public int getDeck2() {
        return deck2;
    }

    public void setDeck2(int deck2) {
        this.deck2 = deck2;
    }

}
