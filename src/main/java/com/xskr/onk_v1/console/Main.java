package com.xskr.onk_v1.console;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xskr.sjb_v1.core.Engine;
import com.xskr.sjb_v1.model.DataPack;
import com.xskr.sjb_v1.model.Finger;

import java.io.*;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws IOException {
        try(
            InputStreamReader inputStreamReader = new InputStreamReader(System.in);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            OutputStreamWriter outputStreamReader = new OutputStreamWriter(System.out);
            PrintWriter printWriter = new PrintWriter(outputStreamReader);
        ){
            ObjectMapper objectMapper = new ObjectMapper();
            Engine engine = new Engine();
            while(true){
                String line = bufferedReader.readLine();
                int colonIndex = line.indexOf(':');
                String playerName = line.substring(0, colonIndex);
                String command = line.substring(colonIndex + 1);
                Finger finger = Finger.valueOf(command);
                engine.action(playerName, finger);
                Map<String, DataPack> results = engine.calc();
                if(results != null){
                    String message = objectMapper.writeValueAsString(results);
                    printWriter.append(message);
                    printWriter.flush();
                }
            }
        }
    }
}
