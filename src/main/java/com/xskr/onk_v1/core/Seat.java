package com.xskr.onk_v1.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.xskr.common.XskrMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * 座位
 * -1: 是默认的observer的座位
 * 0到(房间可用卡牌数量-3): 是场上玩家的座位
 * 其余座位也是无效座位
 * 因为桌游不同于以往游戏，可能要有人会临时退出或加入，实际上桌游的进展是以玩家座位为不变的基础进行的
 */
public class Seat{
	//座位上的玩家如果为null说明该座位没有人坐
	private String userName;
	//该座位的初始卡牌
	private Card initializeCard;
	//该座位的当前卡牌(卡牌可能会经历某些交换操作)
	private Card card;
	//该座位是否声明已经准备好可以开始了
	private boolean ready;
	//该座位的玩家投票到某个座位的玩家
	private Integer voteSeat;
	//该座位玩家被投票的次数
	private int votedCount;
	//该玩家的关键信息，供断线重连时提供
	private List<XskrMessage> keyMessages = new ArrayList();

	//一局游戏结束重置玩家状态
	public void reset(){
	    initializeCard = null;
	    card = null;
	    ready = false;
	    voteSeat = null;
//	    votedCount = 0;
	    keyMessages.clear();
    }
	public String getUserName() {
		return userName;
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

	public void setUserName(String userName) {
		this.userName = userName;
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

//	@JsonIgnore
//	public Room getRoom() {
//		return room;
//	}
//
//	public void setRoom(Room room) {
//		this.room = room;
//	}
//
//	@Override
//	public int compareTo(Seat o) {
//		if(o == null){
//			return 1;
//		}else{
//		    if(getLocation() != null && o.getLocation() != null) {
//                //如果都有座位号就按照座位号排序，作为player
//                return Integer.compare(getLocation(), o.getLocation());
//            }else{
//		        //否则按照姓名排序，作为observer
//		        return getUserName().compareTo(o.getUserName());
//            }
//		}
//	}

    @Override
    public String toString() {
        return "Seat{" +
                "userName='" + userName + '\'' +
                ", card=" + card +
                ", voteSeat=" + voteSeat +
                '}';
    }
}
