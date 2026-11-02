package com.bondtradex.mcp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DiagnosticsMcpServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(
                DiagnosticsMcpServerApplication.class,
                args
        );
    }
}