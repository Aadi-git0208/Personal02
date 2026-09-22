package com.apexcare.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class MedicurexApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(MedicurexApiGatewayApplication.class, args);
    }
}
