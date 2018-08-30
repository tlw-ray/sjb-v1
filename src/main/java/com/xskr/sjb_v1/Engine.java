package com.xskr.sjb_v1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

public class Engine {

    private Logger logger =LoggerFactory.getLogger(getClass());
    private static Map<String, String> playerFingerMap = new HashMap();

    public String enter = "\n";

    public String play(String line) throws IOException {
        String result;
        //只能使用小写字母
        logger.debug("line: {}", line);
        int colonPosition = line.indexOf(':');
        if(colonPosition >= 0){
            String playerName = line.substring(0, colonPosition).trim();
            logger.debug("player name: {}", playerName);
            String playerCommand = line.substring(colonPosition + 1).trim();
            logger.debug("player command: {}", playerCommand);
            playerFingerMap.put(playerName, null);
            if(playerCommand.equals("bye")){
                playerFingerMap.remove(playerName);
            }else if(playerCommand.equals("s") ||
                    playerCommand.equals("j") ||
                    playerCommand.equals("b")){
                playerFingerMap.put(playerName, playerCommand);
            }else{
                // 不支持的指令，提示只能输入s(石头), j(剪刀), b(布), bye(退出)
                result = String.format("%s:只能输入'%s'\n", playerName, playerCommand);
            }
            // 玩家数量超过1人时, 计算输赢
            if(playerFingerMap.size() > 1) {
                result = calc();
            }else{
                result = null;
            }
        }else{
            result = null;
        }
        return result;
    }

    private String calc() throws IOException {
        // 1. 如果还有人没有输入信息就不能开始计算
        for(Map.Entry<String, String> entry: playerFingerMap.entrySet()){
            if(entry.getValue() == null){
                return null;
            }
        }
        // 2. 开始计算
        // 2.1 将所有的输入分组, 并计算出win和los的值(s | j | b)
        String win = null;
        String los = null;
        Set valueSet = new HashSet(playerFingerMap.values());
        if(valueSet.size() == 2){
            // 2.1.1 如果恰好能分为两组则存在输赢
            if(valueSet.contains("s") && valueSet.contains("j")){
                win = "s";
                los = "j";
            }else if(valueSet.contains("s") && valueSet.contains("b")){
                win = "b";
                los = "s";
            }else if(valueSet.contains("j") && valueSet.contains("b")){
                win = "j";
                los = "b";
            }else{
                //错误的状态
                logger.error("错误状态: {}", Arrays.toString(valueSet.toArray()));
            }
        }else{
            // 2.1.2 否则平局
        }
        logger.debug("win = {}, los = {}", win, los);
        // 3. 输出并清除本局状态
        String result = "";
        String outcome;
        for(Map.Entry<String, String> entry: playerFingerMap.entrySet()){
            if(entry.getValue().equals(win)){
                outcome = "胜";
            }else if (entry.getValue().equals(los)){
                outcome = "负";
            }else{
                outcome = "平";
            }
            String player = entry.getKey();
            String finger = translateFinger(entry.getValue());
            String message = String.format("%s,\t%s,\t%s", player, finger, outcome);
            result += (message + enter);
            logger.debug(message);
            entry.setValue(null);
        }
        return result;
    }

    public String translateFinger(String value){
        if(value != null){
            if(value.equals("s")){
                return "石头";
            }else if(value.equals("j")){
                return "剪刀";
            }else if(value.equals("b")){
                return "布  ";
            }else{
                return value;
            }
        }else{
            return "UNKNOWN";
        }
    }

    public String getEnter() {
        return enter;
    }

    public void setEnter(String enter) {
        this.enter = enter;
    }
}
