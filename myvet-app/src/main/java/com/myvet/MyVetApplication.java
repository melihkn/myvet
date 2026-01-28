package com.myvet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.myvet")
public class MyVetApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyVetApplication.class, args);
    }
}