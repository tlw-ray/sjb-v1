package com.xskr.onk_v1.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.xskr.common.XskrMessage;

import java.util.ArrayList;
import java.util.List;

public class Player implements Comparable<Player>{

	private String name;
	private Card initializeCard;
	private Card card;
	private Integer seat;
	private boolean ready;
	//该玩家投票到某个座位的玩家
	private Integer voteSeat;
	//该玩家被投票的次数
	private int votedCount;
	//该玩家的关键信息，供断线重连时提供
	private List<XskrMessage> keyMessages = new ArrayList();
	//该玩家当前所在房间
	private Room room;
	public Player(String name) {
		super();
		this.name = name.trim();
	}

	//一局游戏结束重置玩家状态
	public void reset(){
	    initializeCard = null;
	    card = null;
	    ready = false;
	    voteSeat = null;
	    votedCount = 0;
	    keyMessages.clear();
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

	public Integer getSeat() {
		return seat;
	}

	public void setSeat(Integer seat) {
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

	public Card getInitializeCard() {
		return initializeCard;
	}

	public void setInitializeCard(Card initializeCard) {
		this.initializeCard = initializeCard;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<XskrMessage> getKeyMessages() {
		return keyMessages;
	}

	public void setKeyMessages(List<XskrMessage> keyMessages) {
		this.keyMessages = keyMessages;
	}

	public ClientAction getLastAction() {
	    if(keyMessages.size() > 0){
	        return keyMessages.get(keyMessages.size() - 1).getAction();
        }else{
	        return null;
        }
	}

	@JsonIgnore
	public Room getRoom() {
		return room;
	}

	public void setRoom(Room room) {
		this.room = room;
	}

	@Override
	public int compareTo(Player o) {
		if(o == null){
			return 1;
		}else{
		    if(getSeat() != null && o.getSeat() != null) {
                //如果都有座位号就按照座位号排序，作为player
                return Integer.compare(getSeat(), o.getSeat());
            }else{
		        //否则按照姓名排序，作为observer
		        return getName().compareTo(o.getName());
            }
		}
	}

    @Override
    public String toString() {
        return "Player{" +
                "name='" + name + '\'' +
                ", card=" + card +
                ", seat=" + seat +
                ", voteSeat=" + voteSeat +
                ", votedCount=" + votedCount +
                '}';
    }
}
