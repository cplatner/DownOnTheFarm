package com.platner.farm.models;

public enum Actions {
    plow("plow"),
    plant("plant"),
    irrigate("irrigate"),
    harvest("harvest");

    public final String label;

    Actions(String label) {
        this.label = label;
    }
}
