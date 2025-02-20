package com.platner.farm.models;

public enum Status {
    UP("UP"),
    DOWN("DOWN");

    public final String label;

    Status(String label) {
        this.label = label;
    }
}
