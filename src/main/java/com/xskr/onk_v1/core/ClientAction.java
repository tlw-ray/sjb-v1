package com.xskr.onk_v1.core;
//用于告知客户端行动身份特征
public enum ClientAction {

    SINGLE_WOLF_ACTION, DRUNK_ACTION, ROBBER_ACTION, SEER_ACTION, TROUBLEMAKER_ACTION,  //玩家身份应进行的行动

    VOTE_ACTION,                    //投票

    HUNTER_VOTE_ACTION,             //猎人投票

    ROOM_CHANGED,                   //有玩家进入游戏或改变了准备状态需要刷新玩家列表

    RECONNECT,                      //断线重连

    LEAVE_ROOM,                     //此玩家离开房间事件

    NEW_GAME,                       //新一局游戏开始

    GAME_FINISH                     //一局游戏结束，解除所有玩家的准备状态
}
