package com.xskr.onk_v1.core;

import java.util.HashSet;
import java.util.Set;

public enum Camp {

    WOLF, VILLAGER, TANNER;

    public static boolean isWolfCamp(Card card){
        return card == Card.WEREWOLF_1
                || card == Card.WEREWOLF_2
                || card == Card.MINION;
    }

    public static boolean isVillagerCamp(Card card){
        return !isWolfCamp(card) && !isTannerCamp(card);
    }

    public static boolean isTannerCamp(Card card){
        return card == Card.TANNER;
    }

    public static Set<Card> getCards(Camp camp){
        Set<Card> result = new HashSet();
        for(Card card : Card.values()){
            switch(camp){
                case WOLF: if(isWolfCamp(card)) result.add(card); break;
                case TANNER: if(isTannerCamp(card)) result.add(card); break;
                case VILLAGER: if(isVillagerCamp(card)) result.add(card); break;
            }
        }
        return result;
    }
}
