package com.medicurex.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class MedicurexOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(MedicurexOrderApplication.class, args);
    }
}
