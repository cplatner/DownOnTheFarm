package com.platner.farm.models;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Status to be used by health monitors
 */
@Getter
@RequiredArgsConstructor
public final class HealthStatus {
    @NonNull
    private Status status;
}
