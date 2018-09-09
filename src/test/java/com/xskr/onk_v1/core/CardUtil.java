package com.xskr.onk_v1.core;

import java.util.ArrayList;
import java.util.List;

public class CardUtil {
    public static List<Card> getCards(int count){
        //检查count的合理性
        int minCardCount = 7;
        int maxCardCount = Card.values().length - 1;
        if(count > maxCardCount){
            throw new RuntimeException("Card数量不能大于: " + maxCardCount);
        }if(count < minCardCount){
            throw new RuntimeException("Card数量不能小于: " + minCardCount);
        }else{
            //按顺序获得去除化身幽灵外按顺序获取身份
            List<Card> cards = new ArrayList();
            for(Card card:Card.values())cards.add(card);
            cards.remove(0);
            for(int i = 0; i < maxCardCount - count; i++){
                cards.remove(cards.size() - 1);
            }
            return cards;
        }
    }
}
