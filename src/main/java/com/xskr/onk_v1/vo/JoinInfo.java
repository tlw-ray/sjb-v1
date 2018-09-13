package com.xskr.onk_v1.vo;

import java.util.TreeMap;

public class JoinInfo {
    int roomID;
    String playerName;
    TreeMap<Integer, String> seatPlayerNameMap;

    public int getRoomID() {
        return roomID;
    }

    public void setRoomID(int roomID) {
        this.roomID = roomID;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public TreeMap<Integer, String> getSeatPlayerNameMap() {
        return seatPlayerNameMap;
    }

    public void setSeatPlayerNameMap(TreeMap<Integer, String> seatPlayerNameMap) {
        this.seatPlayerNameMap = seatPlayerNameMap;
    }
}
