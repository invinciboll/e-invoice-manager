package com.invinciboll;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.invinciboll.configuration.AppConfig;


@SpringBootApplication(scanBasePackages = "com.invinciboll")
public class ServerApplication {

    public static void main(String[] args) {
        try {
            AppConfig config = AppConfig.getInstance("config.properties");
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SpringApplication.run(ServerApplication.class, args);
    }

}
