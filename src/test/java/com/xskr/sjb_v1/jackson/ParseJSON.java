package com.xskr.sjb_v1.jackson;

import com.alibaba.fastjson.JSONArray;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;

public class ParseJSON {
    @Test
    public void test() throws IOException {
        String test = "[\"aa\",\"bb\"]";
        Object array = JSONArray.parse(test);
        System.out.println(array);

    }
}
