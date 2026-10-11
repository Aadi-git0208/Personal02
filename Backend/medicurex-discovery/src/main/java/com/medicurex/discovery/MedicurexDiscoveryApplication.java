package com.medicurex.discovery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class MedicurexDiscoveryApplication {

    public static void main(String[] args) {
        SpringApplication.run(MedicurexDiscoveryApplication.class, args);
    }
}
