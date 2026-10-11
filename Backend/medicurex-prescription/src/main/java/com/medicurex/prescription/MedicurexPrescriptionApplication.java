package com.medicurex.prescription;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class MedicurexPrescriptionApplication {

    public static void main(String[] args) {
        SpringApplication.run(MedicurexPrescriptionApplication.class, args);
    }
}
