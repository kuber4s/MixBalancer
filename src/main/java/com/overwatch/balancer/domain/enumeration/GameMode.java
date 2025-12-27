package com.overwatch.balancer.domain.enumeration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GameMode {
    CONTROL("Control", "🔵"),
    ESCORT("Escort", "🚚"),
    FLASHPOINT("Flashpoint", "⚡"),
    HYBRID("Hybrid", "🔀"),
    PUSH("Push", "🤖");

    private final String displayName;
    private final String emoji;

    @Override
    public String toString() {
        return emoji + " " + displayName;
    }
}
