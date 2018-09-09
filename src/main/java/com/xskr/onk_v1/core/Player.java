package com.xskr.onk_v1.core;

import java.util.Objects;

public class Player implements Comparable<Player>{
	
	private String name;
	private Card card;
	private int seat;
	private boolean ready;
	private Integer voteSeat;	//该玩家投票到某个座位的玩家
	private int votedCount;		//该玩家被投票的次数
	
	public Player(String name) {
		super();
		this.name = name.trim();
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

	public boolean isReady() {
		return ready;
	}

	void setReady(boolean ready) {
		this.ready = ready;
	}

	public int getSeat() {
		return seat;
	}

	public void setSeat(int seat) {
		this.seat = seat;
	}

	public Integer getVoteSeat() {
		return voteSeat;
	}

	public void setVoteSeat(Integer voteSeat) {
		this.voteSeat = voteSeat;
	}

	public int getVotedCount() {
		return votedCount;
	}

	public void setVotedCount(int votedCount) {
		this.votedCount = votedCount;
	}

	public void beVote(){
		votedCount++;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Player player = (Player) o;
		return Objects.equals(name, player.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name);
	}

	@Override
	public int compareTo(Player o) {
		if(o == null){
			return 1;
		}else{
			return getName().compareTo(o.getName());
		}
	}
}
