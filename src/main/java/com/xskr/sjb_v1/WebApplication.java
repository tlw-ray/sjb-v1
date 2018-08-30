package com.xskr.sjb_v1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class WebApplication {

    public static void main(String[] args){
        SpringApplication.run(WebApplication.class, args);
    }

}
