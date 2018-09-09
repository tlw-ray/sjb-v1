package com.xskr.sjb_v1.model;

import java.util.Hashtable;
import java.util.Map;

public class DataPack {

    private Finger finger;
    private Ends ends;
    private Map<Ends, Integer> endsCount = new Hashtable();
    private Map<Finger, Integer> fingerCount = new Hashtable();

    public DataPack(){
        //初始化统计数据为0
        for(Finger finger:Finger.values()) fingerCount.put(finger, new Integer(0));
        for(Ends ends:Ends.values()) endsCount.put(ends, new Integer(0));
    }

    public Finger getFinger() {
        return finger;
    }

    public void setFinger(Finger finger) {
        this.finger = finger;
    }

    public Ends getEnds() {
        return ends;
    }

    public void setEnds(Ends ends) {
        this.ends = ends;
    }

    public Map<Ends, Integer> getEndsCount() {
        return endsCount;
    }

    public void setEndsCount(Map<Ends, Integer> endsCount) {
        this.endsCount = endsCount;
    }

    public Map<Finger, Integer> getFingerCount() {
        return fingerCount;
    }

    public void setFingerCount(Map<Finger, Integer> fingerCount) {
        this.fingerCount = fingerCount;
    }
}
