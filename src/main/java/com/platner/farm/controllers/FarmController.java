package com.platner.farm.controllers;

import com.platner.farm.models.FarmAction;
import com.platner.farm.models.FarmStatus;
import com.platner.farm.repository.FarmStatusRepository;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.Refill;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
@RequestMapping("/api/v1")
public class FarmController {
    private final Bucket tokenBucket;

    @Autowired
    private FarmStatusRepository repository;
    
    public FarmController() {
        tokenBucket = Bucket.builder()
                .addLimit(Bandwidth.classic(2, Refill.greedy(2, Duration.ofMinutes(1))))
                .build();
    }

    @Operation(summary = "Do some work on the farm, and find out what happens")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "What is happening on the farm",
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = FarmStatus.class))}),
            @ApiResponse(responseCode = "400", description = "Invalid farm action", content = @Content),
            @ApiResponse(responseCode = "429", description = "Too many requests", content = @Content)})
    @PostMapping(value = "farm", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<FarmStatus> farm(
            @Parameter(description = "Kind of farm action")
            @RequestBody @NonNull  FarmAction action) {

        ConsumptionProbe probe = tokenBucket.tryConsumeAndReturnRemaining(1);
        if (probe.isConsumed()) {
            FarmStatus status = repository.getStatus(action);
            return ResponseEntity.ok()
                    // Rate Limit headers taken from https://datatracker.ietf.org/doc/draft-ietf-httpapi-ratelimit-headers/
                    // and https://medium.com/@guillaume.viguierjust/rate-limiting-your-restful-api-3148f8e77248
                    // Both old and soon-to-be-new are sent
                    .header("X-RateLimit-Remaining", Long.toString(probe.getRemainingTokens()))
                    .header("RateLimit-Remaining", Long.toString(probe.getRemainingTokens()))
                    .body(status);
        }

        long waitForRefill = probe.getNanosToWaitForRefill() / 1_000_000_000;
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header("X-RateLimit-Retry-After-Seconds", String.valueOf(waitForRefill))
                .header("Retry-After", String.valueOf(waitForRefill))
                .build();
    }

}
