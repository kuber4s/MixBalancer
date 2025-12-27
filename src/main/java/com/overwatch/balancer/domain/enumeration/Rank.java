package com.overwatch.balancer.domain.enumeration;

import lombok.Getter;

@Getter
public enum Rank {
    BRONZE("Bronze", "B", 1000),
    SILVER("Silver", "S", 1500),
    GOLD("Gold", "G", 2000),
    PLATINUM("Platinum", "P", 2500),
    DIAMOND("Diamond", "D", 3000),
    MASTER("Master", "M", 3500),
    GRANDMASTER("Grandmaster", "GM", 4000),
    CHAMPION("Champion", "C", 4500);

    private final String displayName;
    private final String shortName;
    private final int minSR;

    Rank(String displayName, String shortName, int minSR) {
        this.displayName = displayName;
        this.shortName = shortName;
        this.minSR = minSR;
    }

    public static Rank fromSR(int sr) {
        if (sr < 1000) return BRONZE;
        if (sr >= 4500) return CHAMPION;

        Rank[] ranks = values();
        for (int i = ranks.length - 1; i >= 0; i--) {
            if (sr >= ranks[i].minSR) {
                return ranks[i];
            }
        }
        return BRONZE;
    }
}
