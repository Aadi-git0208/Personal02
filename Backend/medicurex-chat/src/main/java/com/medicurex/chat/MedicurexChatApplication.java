package com.medicurex.chat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class MedicurexChatApplication {

    public static void main(String[] args) {
        SpringApplication.run(MedicurexChatApplication.class, args);
    }
}
