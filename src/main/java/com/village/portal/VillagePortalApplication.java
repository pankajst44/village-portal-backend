package com.village.portal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class VillagePortalApplication {

    public static void main(String[] args) {
        SpringApplication.run(VillagePortalApplication.class, args);
    }
}
