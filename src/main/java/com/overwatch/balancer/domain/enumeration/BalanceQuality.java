package com.overwatch.balancer.domain.enumeration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BalanceQuality {
    EXCELLENT("Excellent", "🟢", 1.0),
    GOOD("Good", "🟡", 0.8),
    FAIR("Fair", "🟠", 0.6),
    POOR("Poor", "🔴", 0.4);

    private final String displayName;
    private final String emoji;
    private final double score;

    @Override
    public String toString() {
        return emoji + " " + displayName;
    }
}
