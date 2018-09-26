package com.xskr.onk_v1.core;

import java.util.Set;

//投票统计结果
public class VoteStat {
    //是否有狼
    boolean hasWolfInPlayers;
    //是否有猎人
    boolean hasHunterInPlayers;
    //最大得票数
    int maxVoteCount;
    //获得最大得票数的玩家集合
    Set<Seat> votedPlayer;

    public VoteStat(boolean hasWolfInPlayers, boolean hasHunterInPlayers, int maxVoteCount, Set<Seat> votedPlayer) {
        this.hasWolfInPlayers = hasWolfInPlayers;
        this.hasHunterInPlayers = hasHunterInPlayers;
        this.maxVoteCount = maxVoteCount;
        this.votedPlayer = votedPlayer;
    }

    public boolean hasWolfInPlayers() {
        return hasWolfInPlayers;
    }

    public boolean onlyVotedTanner() {
        return votedPlayer.size() == 1 && votedPlayer.iterator().next().getCard() == Card.TANNER;
    }

    public boolean voted(Card card){
        for(Seat player:votedPlayer){
            if(player.getCard() == card){
                return true;
            }
        }
        return false;
    }

    public boolean hasHunterInPlayers() {
        return hasHunterInPlayers;
    }

    public int getMaxVoteCount() {
        return maxVoteCount;
    }

    public Set<Seat> getVotedPlayer() {
        return votedPlayer;
    }
}
