package com.apexcare.appointment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class MedicurexAppointmentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MedicurexAppointmentServiceApplication.class, args);
    }
}
