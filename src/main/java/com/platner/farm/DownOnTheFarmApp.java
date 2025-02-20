package com.platner.farm;

import com.platner.farm.models.HealthStatus;
import com.platner.farm.models.Status;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class DownOnTheFarmApp {

    public static void main(String[] args) {
        SpringApplication.run(DownOnTheFarmApp.class, args);
    }

    @GetMapping(value = "/ping", produces = MediaType.TEXT_PLAIN_VALUE)
    public String ping() {
        return "pong";
    }

    // Skipping actuator framework
    @GetMapping(value = "/health", produces = MediaType.APPLICATION_JSON_VALUE)
    public HealthStatus health() {
        return new HealthStatus(Status.UP);
    }

}
