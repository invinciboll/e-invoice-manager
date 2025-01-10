package com.invinciboll;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
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
