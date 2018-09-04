package com.xskr.sjb_v1;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

@Controller
public class ZoomController {
    @Scheduled(fixedRate = 5000)
    public void playerInformation() {

    }
}
