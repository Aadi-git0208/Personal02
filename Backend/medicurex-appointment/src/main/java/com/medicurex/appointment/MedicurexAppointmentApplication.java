package com.medicurex.appointment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class MedicurexAppointmentApplication {

    public static void main(String[] args) {
        SpringApplication.run(MedicurexAppointmentApplication.class, args);
    }
}
