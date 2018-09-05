package com.xskr.room;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.jws.WebMethod;

@RestController
public class ZoomController {

    @RequestMapping(value = "/zoom", method = RequestMethod.POST)
    public void createXxxZoom(){

    }

    @RequestMapping("/zoom/{uuid}")
    public void joinZoom(@PathVariable String uuid){

    }
}
