package com.xskr.sjb_v1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class Console {

    private static Logger logger = LoggerFactory.getLogger(Console.class);

    public static void main(String[] args) throws IOException {
        try(
            InputStreamReader inputStreamReader = new InputStreamReader(System.in);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            OutputStreamWriter outputStreamReader = new OutputStreamWriter(System.out);
            PrintWriter printWriter = new PrintWriter(outputStreamReader);
        ){
            Engine engine = new Engine();
            while(true){
                String line = bufferedReader.readLine().trim().toLowerCase();
                String result = engine.play(line);
                if(result != null){
                    System.out.println(result);
                }
            }
        }
    }


}
