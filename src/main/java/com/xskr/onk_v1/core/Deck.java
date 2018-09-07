package com.xskr.onk_v1.core;

import java.util.Random;

public class Deck {

	public static final Card[] CARD_TEMPLATE = new Card[]{
		Card.DOPPELGANGER,
		Card.WEREWOLF_1, Card.WEREWOLF_2, Card.MINION,
		Card.MASON_1, Card.MASON_2,
		Card.SEER, Card.ROBBER, Card.TROUBLEMAKER, Card.DRUNK, Card.INSOMNIAC,
		Card.VILLAGER_1, Card.VILLAGER_2, Card.VILLAGER_3,
		Card.HUNTER, Card.TANNER
	};
	  
    private Card cards[];
    private int currentCount;
    
    public Deck(Card[] cards){
    	this.cards = cards;
    }
	
	// 洗牌
	public void shuffle(int numberOfTime){
	       Random rand= new Random();

	        for(int i=0;i<numberOfTime;i++){
	            int m=rand.nextInt(currentCount);
	            int n=rand.nextInt(currentCount);

	            switchCard(m, n);
	        }
	}
	
	// 发牌
	public Card deal(){
		return cards[--currentCount];
	}
	
	// 重置
	public void resetdesk(){
		currentCount = cards.length;
	}
	
	// 交换现有牌库中的两张牌
	private void switchCard(int m, int n){
        Card temp=cards[m];
        cards[m]=cards[n];
        cards[n]=temp;
	}

	// 输出当前牌库中的内容
	public String toString(){
        StringBuilder sb=new StringBuilder();
        for(int i=0;i < currentCount;i++){
            sb.append(cards[i]);
            sb.append(" ");
        }
	    return sb.toString();
	}
}
