package com.xskr.sjb_v1.core;

import com.xskr.sjb_v1.model.Ends;
import com.xskr.sjb_v1.model.Finger;
import com.xskr.sjb_v1.model.DataPack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class Engine {

    private Logger logger =LoggerFactory.getLogger(getClass());

    private Map<String, DataPack> playerDataPack = new TreeMap();

    private String enter = "\n";

    private Date lastCalc = new Date();
    private long timeout = 1000*5;      //每5秒一局

    public void action(String playerName, Finger finger) {
        DataPack dataPack = playerDataPack.get(playerName);
        if(dataPack == null){
            dataPack = new DataPack();
            playerDataPack.put(playerName, dataPack);
        }
        dataPack.setFinger(finger);
    }

    //返回玩家对应的数据(本场猜拳, 输赢, 以往猜拳数据统计, 输赢统计)
    public Map<String, DataPack> calc(){
        // 如果上一次计算时间到这次已经超过了超时时间，那么可以开始一次计算
        Date now = new Date();
        if(now.getTime() - lastCalc.getTime() > timeout) {

            // 判断已出示猜拳的人数
            int fingered = 0;
            for (Map.Entry<String, DataPack> entry : playerDataPack.entrySet()) {
                if (entry.getValue() != null && entry.getValue().getFinger() != null) fingered++;
            }

            logger.debug("fingered: " + fingered);

            // 已出示猜拳的总人数大于1人则可以判定输赢，还没有猜拳的人算超时未出拳
            if(fingered > 1) {

                // 将所有的输入分组, 并计算出win和los的值(s | j | b)
                Finger victory = null;
                Finger defeat = null;

                Set fingerSet = new HashSet();
                for (Map.Entry<String, DataPack> entry : playerDataPack.entrySet()) {
                    fingerSet.add(entry.getValue().getFinger());
                }
                fingerSet.remove(null);
                if (fingerSet.size() == 2) {

                    // 如果去掉未出的恰好能分为两组则存在输赢
                    if (fingerSet.contains(Finger.ROCK) && fingerSet.contains(Finger.SCISSORS)) {
                        victory = Finger.ROCK;
                        defeat = Finger.SCISSORS;
                    } else if (fingerSet.contains(Finger.ROCK) && fingerSet.contains(Finger.PAPER)) {
                        victory = Finger.PAPER;
                        defeat = Finger.ROCK;
                    } else if (fingerSet.contains(Finger.SCISSORS) && fingerSet.contains(Finger.PAPER)) {
                        victory = Finger.SCISSORS;
                        defeat = Finger.PAPER;
                    } else {
                        //错误的状态
                        logger.error("Error status: {}", Arrays.toString(fingerSet.toArray()));
                    }
                } else {
                    // 否则平局
                }
                logger.debug("victory = {}, defeat = {}", victory, defeat);

                // 输出并清除本局状态
                Ends ends;
                for (Map.Entry<String, DataPack> entry : playerDataPack.entrySet()) {
                    //判断玩家的输赢
                    if(entry.getValue() == null || entry.getValue().getFinger() == null){
                        ends = Ends.GIVE_UP;
                    }else {
                        Finger finger = entry.getValue().getFinger();
                        if (finger == victory) {
                            ends = Ends.VICTORY;
                        } else if (finger == defeat) {
                            ends = Ends.DEFEAT;
                        } else {
                            ends = Ends.TIE;
                        }
                    }
                    String playerName = entry.getKey();
                    Finger finger = entry.getValue().getFinger();

                    //记录本局分数
                    DataPack dataPack = playerDataPack.get(playerName);
//                    if(dataPack == null){
//                        dataPack = new DataPack();
//                        playerDataPack.put(playerName, dataPack);
//                    }
                    dataPack.setEnds(ends);
                    dataPack.setFinger(finger);
                    logger.debug("player: {}, finger: {}, ends: {}", playerName, finger, ends);

                    //将本局分数统计入以往
                    Map<Ends, Integer> endsCount = dataPack.getEndsCount();
                    int newEndsCount = endsCount.get(ends) + 1;
                    dataPack.getEndsCount().put(ends, newEndsCount);
                    Map<Finger, Integer> fingerCount = dataPack.getFingerCount();
                    int newFingerCount = fingerCount.get(finger) + 1;
                    dataPack.getFingerCount().put(finger, newFingerCount);

                    //玩家数据包更新
                    playerDataPack.put(playerName, dataPack);
                }
                return playerDataPack;
            }
        }
        return null;
    }

    // 清除已经用过的猜拳状态为空，为下一局做准备
    public void clearFinger(){
        for(Map.Entry<String, DataPack> entry: playerDataPack.entrySet()){
            DataPack dataPack = entry.getValue();
            if(dataPack != null){
                dataPack.setFinger(null);
            }
        }
    }

    public String getEnter() {
        return enter;
    }

    public void setEnter(String enter) {
        this.enter = enter;
    }
}
