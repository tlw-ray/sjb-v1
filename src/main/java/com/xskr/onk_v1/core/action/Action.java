package com.xskr.onk_v1.core.action;

import com.xskr.onk_v1.core.Player;
import com.xskr.onk_v1.core.Room;

public interface Action {
    void perform(Player player, Room room);
//    String getNightMessage();
//    String getMorningMessage();
}
