package com.xskr.common;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CommonController {
    @RequestMapping(path = "/who")
    public String who(){
        return WebUtil.getCurrentUserName();
    }
}
