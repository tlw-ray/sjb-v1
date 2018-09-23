package com.xskr.onk_v1.core;

import java.util.ArrayList;
import java.util.List;

public class CardUtil {
    public static Card[] getCards(int count){
        //检查count的合理性
        int minCardCount = 7;
        int maxCardCount = Card.values().length - 1;
        if(count > maxCardCount){
            throw new RuntimeException("Card数量不能大于: " + maxCardCount);
        }if(count < minCardCount){
            throw new RuntimeException("Card数量不能小于: " + minCardCount);
        }else{
            //按顺序获得去除化身幽灵外按顺序获取身份
            Card[] cards = new Card[count];
            int idx = 0;
            for(Card card:Card.values()){
                if(idx == 0){

                }else{
                    cards[idx - 1] = card;
                }
                idx++;
                if(idx > count){
                    break;
                }
            }
            return cards;
        }
    }
}
