package com.overwatch.balancer.domain.model;

import com.overwatch.balancer.domain.enumeration.Rank;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class SkillRating implements Comparable<SkillRating> {

    public static final int MIN_VALUE = 0;
    public static final int MAX_VALUE = 5000;

    public static final SkillRating ZERO = new SkillRating(0);

    private final int value;

    private SkillRating(int value) {
        this.value = value;
    }

    public static SkillRating of(int value) {
        if (value < MIN_VALUE || value > MAX_VALUE) {
            throw new IllegalArgumentException(
                    "SR must be between " + MIN_VALUE + " and " + MAX_VALUE + ", got: " + value
            );
        }
        return value == 0 ? ZERO : new SkillRating(value);
    }

    public boolean isActive() {
        return value > 0;
    }

    public Rank getRank() {
        return Rank.fromSR(value);
    }

    public int getDivision() {
        if (value == 0) return 0;

        Rank rank = getRank();
        int srInRank = value - rank.getMinSR();
        return 5 - (srInRank / 100);
    }

    public int difference(SkillRating other) {
        return Math.abs(this.value - other.value);
    }

    public String toDisplayString() {
        if (value == 0) {
            return "Not playing";
        }
        return getRank().getDisplayName() + " " + getDivision() + " (" + value + ")";
    }

    public String toShortString() {
        if (value == 0) return "-";
        return getRank().getShortName() + getDivision();
    }

    @Override
    public int compareTo(SkillRating other) {
        return Integer.compare(this.value, other.value);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

}
