package com.xskr.onk_v1.core;

import com.xskr.onk_v1.core.action.Action;

public class Player implements Comparable<Player>{
	
	private int id;
	private String name;
	private Card card;
	private ShowCard showCard;          //展示给强盗孤狼玩家看的卡牌
	private Card swapCard;
	private int seat;
	private boolean ready;
	private Action action;
	
	public Player(int id, String name) {
		super();
		this.id = id;
		this.name = name.trim();
	}
	
	public Integer getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Card getCard() {
		return card;
	}

	public void setCard(Card card) {
		this.card = card;
	}

    public ShowCard getShowCard() {
        return showCard;
    }

    public void setShowCard(ShowCard showCard) {
        this.showCard = showCard;
    }

    public Card getSwapCard() {
		return swapCard;
	}

	public void setSwapCard(Card swapCard) {
		this.swapCard = swapCard;
	}

	public boolean isReady() {
		return ready;
	}

	public void setReady(boolean ready) {
		this.ready = ready;
	}

	public int getSeat() {
		return seat;
	}

	public void setSeat(int seat) {
		this.seat = seat;
	}

	public Action getAction() {
		return action;
	}

	public void setAction(Action action) {
		this.action = action;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Player other = (Player) obj;
		if (id != other.id)
			return false;
		return true;
	}

	public int compareTo(Player o) {
		return getId().compareTo(o.getId());
	}
	
}
