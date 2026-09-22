package com.apexcare.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class MedicurexOrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MedicurexOrderServiceApplication.class, args);
    }
}
