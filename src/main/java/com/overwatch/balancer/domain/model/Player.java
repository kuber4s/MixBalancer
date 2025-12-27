package com.overwatch.balancer.domain.model;

import com.overwatch.balancer.domain.enumeration.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.Instant;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "players")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
public class Player {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private String id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "tank_sr", nullable = false)
    private int tankSR;

    @Column(name = "dps_sr", nullable = false)
    private int dpsSR;

    @Column(name = "support_sr", nullable = false)
    private int supportSR;

    @Column(name = "games_played", nullable = false)
    private int gamesPlayed;

    @Column(name = "games_skipped", nullable = false)
    private int gamesSkipped;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Player(String id, String name) {
        this.id = id;
        this.name = name;
        this.tankSR = 0;
        this.dpsSR = 0;
        this.supportSR = 0;
        this.gamesPlayed = 0;
        this.gamesSkipped = 0;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public static Player create(String name) {
        return new Player(UUID.randomUUID().toString(), name);
    }

    public void setRating(Role role, int sr) {
        validateSR(sr);
        switch (role) {
            case TANK -> this.tankSR = sr;
            case DPS -> this.dpsSR = sr;
            case SUPPORT -> this.supportSR = sr;
        }
        this.updatedAt = Instant.now();
    }

    public int getRating(Role role) {
        return switch (role) {
            case TANK -> tankSR;
            case DPS -> dpsSR;
            case SUPPORT -> supportSR;
        };
    }

    public SkillRating getSkillRating(Role role) {
        return SkillRating.of(getRating(role));
    }

    public void setRatings(Map<Role, Integer> ratings) {
        ratings.forEach(this::setRating);
    }

    public Map<Role, Integer> getRatings() {
        Map<Role, Integer> ratings = new EnumMap<>(Role.class);
        ratings.put(Role.TANK, tankSR);
        ratings.put(Role.DPS, dpsSR);
        ratings.put(Role.SUPPORT, supportSR);
        return ratings;
    }

    public boolean canPlay(Role role) {
        return getRating(role) > 0;
    }

    public boolean hasActiveRole() {
        return tankSR > 0 || dpsSR > 0 || supportSR > 0;
    }

    public Role getPrimaryRole() {
        Role primary = null;
        int maxSR = 0;

        for (Role role : Role.values()) {
            int sr = getRating(role);
            if (sr > maxSR) {
                maxSR = sr;
                primary = role;
            }
        }

        return primary;
    }

    public int getHighestSR() {
        return Math.max(tankSR, Math.max(dpsSR, supportSR));
    }

    public double getAverageSR() {
        int sum = 0;
        int count = 0;

        for (Role role : Role.values()) {
            int sr = getRating(role);
            if (sr > 0) {
                sum += sr;
                count++;
            }
        }

        return count > 0 ? (double) sum / count : 0;
    }

    public void recordGamePlayed() {
        this.gamesPlayed++;
        this.gamesSkipped = 0;
        this.updatedAt = Instant.now();
    }

    public void recordGameSkipped() {
        this.gamesSkipped++;
        this.updatedAt = Instant.now();
    }

    public int getPriorityScore() {
        return gamesSkipped * 10000 + getHighestSR();
    }

    private void validateSR(int sr) {
        if (sr < SkillRating.MIN_VALUE || sr > SkillRating.MAX_VALUE) {
            throw new IllegalArgumentException(
                    "SR must be between 0 and 5500, got: " + sr
            );
        }
    }

    @Override
    public String toString() {
        Role primary = getPrimaryRole();
        if (primary == null) {
            return name + " (no roles)";
        }
        return primary.getEmoji() + " " + name + " (" + getRating(primary) + ")";
    }

}
