package com.xskr.onk_v1.core;

import java.util.Random;

public class Deck {

    private Card cards[];
    private int currentCount;
    
    public Deck(Card[] cards){
    	this.cards = cards;
    	currentCount = cards.length;
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

	public Card[] getCards(){
    	return cards;
	}
}
