package com.overwatch.balancer.domain.enumeration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {

    TANK("tank", "🛡️", 1, 500),
    DPS("dps", "⚔️", 2, 300),
    SUPPORT("support", "💚", 2, 300);

    private final String name;
    private final String emoji;

    private final int slotsPerTeam;

    private final int maxAcceptableDiff;

    /**
     * Parses role from string.
     *
     * @param input role name (case-insensitive)
     * @return parsed Role
     * @throws IllegalArgumentException if invalid
     */
    public static Role parse(String input) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("Role cannot be empty");
        }

        return switch (input.toLowerCase().trim()) {
            case "tank" -> TANK;
            case "dps" -> DPS;
            case "support", "supp" -> SUPPORT;
            default -> throw new IllegalArgumentException(
                    "Unknown role: '" + input + "'. Valid: tank, dps, support"
            );
        };
    }

    public int getSlotsPerMatch() {
        return slotsPerTeam * 2;
    }

    public double getBalanceWeight() {
        return switch (this) {
            case TANK -> 1.5;
            case DPS -> 1.0;
            case SUPPORT -> 1.2;
        };
    }

    @Override
    public String toString() {
        return emoji + " " + name;
    }

}
