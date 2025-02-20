package com.platner.farm.models;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public final class FarmAction {
    @NonNull
    private Actions action;
}
