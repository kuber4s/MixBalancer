package com.overwatch.balancer.domain.model;

import com.overwatch.balancer.domain.enumeration.BalanceQuality;
import com.overwatch.balancer.domain.enumeration.GameMap;
import com.overwatch.balancer.domain.enumeration.Role;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "matches")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Match {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private String id;

    @Column(name = "lobby_id")
    private String lobbyId;

    @Column(name = "map_name", nullable = false)
    private String mapName;

    @Column(name = "map_mode", nullable = false)
    private String mapMode;

    // Team 1 data (stored as JSON or normalized)
    @Column(name = "team1_name", nullable = false)
    private String team1Name;

    @Column(name = "team1_player_ids", nullable = false, length = 1000)
    private String team1PlayerIds; // comma-separated

    @Column(name = "team1_avg_sr", nullable = false)
    private int team1AvgSR;

    // Team 2 data
    @Column(name = "team2_name", nullable = false)
    private String team2Name;

    @Column(name = "team2_player_ids", nullable = false, length = 1000)
    private String team2PlayerIds; // comma-separated

    @Column(name = "team2_avg_sr", nullable = false)
    private int team2AvgSR;

    // Balance metrics
    @Column(name = "sr_difference", nullable = false)
    private int srDifference;

    @Column(name = "tank_sr_diff", nullable = false)
    private int tankSRDiff;

    @Column(name = "dps_sr_diff", nullable = false)
    private int dpsSRDiff;

    @Column(name = "support_sr_diff", nullable = false)
    private int supportSRDiff;

    @Column(name = "balance_quality", nullable = false)
    private String balanceQuality;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * Transient fields for in-memory operations.
     */
    @Transient
    private Team team1;

    @Transient
    private Team team2;

    @Transient
    private GameMap gameMap;

    @Transient
    private List<Player> queue;

    /**
     * Creates a match from balanced teams.
     */
    public static Match create(Team team1, Team team2, GameMap map, List<Player> queue) {
        Match match = new Match();
        match.id = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        match.createdAt = Instant.now();

        // Store teams
        match.team1 = team1;
        match.team2 = team2;
        match.gameMap = map;
        match.queue = queue;

        // Persist team data
        match.team1Name = team1.getName();
        match.team1PlayerIds = String.join(",", team1.getPlayerIds());
        match.team1AvgSR = (int) team1.getAverageSR();

        match.team2Name = team2.getName();
        match.team2PlayerIds = String.join(",", team2.getPlayerIds());
        match.team2AvgSR = (int) team2.getAverageSR();

        // Map data
        match.mapName = map.getDisplayName();
        match.mapMode = map.getMode().getDisplayName();

        // Calculate metrics
        match.srDifference = Math.abs(match.team1AvgSR - match.team2AvgSR);
        match.tankSRDiff = Math.abs(team1.getTankSR() - team2.getTankSR());
        match.dpsSRDiff = Math.abs(
                (int) team1.getRoleAverageSR(Role.DPS) - (int) team2.getRoleAverageSR(Role.DPS)
        );
        match.supportSRDiff = Math.abs(
                (int) team1.getRoleAverageSR(Role.SUPPORT) - (int) team2.getRoleAverageSR(Role.SUPPORT)
        );

        match.balanceQuality = match.calculateQuality().name();

        return match;
    }

    /**
     * Creates match with auto-selected random map.
     */
    public static Match create(Team team1, Team team2, List<Player> queue) {
        return create(team1, team2, GameMap.random(), queue);
    }

    /**
     * Returns balance quality assessment.
     */
    public BalanceQuality calculateQuality() {
        // Tank difference is critical
        if (tankSRDiff > 600) return BalanceQuality.POOR;

        // Overall SR difference
        if (srDifference <= 30 && tankSRDiff <= 200) return BalanceQuality.EXCELLENT;
        if (srDifference <= 75 && tankSRDiff <= 350) return BalanceQuality.GOOD;
        if (srDifference <= 150 && tankSRDiff <= 500) return BalanceQuality.FAIR;

        return BalanceQuality.POOR;
    }

    /**
     * Gets max role SR difference.
     */
    public int getMaxRoleDiff() {
        return Math.max(tankSRDiff, Math.max(dpsSRDiff, supportSRDiff));
    }

    /**
     * Formats match for display.
     */
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();

        sb.append("# 🎮 Match Ready!\n\n");

        // Map
        sb.append("🗺️ **Map:** ").append(mapName);
        sb.append(" (").append(mapMode).append(")\n\n");

        // Quality
        BalanceQuality quality = calculateQuality();
        sb.append("📊 **Balance:** ").append(quality.getEmoji()).append(" ");
        sb.append(quality.getDisplayName()).append("\n");
        sb.append("📈 **SR Difference:** ").append(srDifference).append("\n");
        sb.append("🆔 **Match ID:** `").append(id).append("`\n\n");

        // Teams
        if (team1 != null && team2 != null) {
            sb.append(team1.toDisplayString());
            sb.append("\n⚔️ **VS** ⚔️\n\n");
            sb.append(team2.toDisplayString());
        } else {
            sb.append("**").append(team1Name).append("** (avg: ").append(team1AvgSR).append(")\n");
            sb.append("vs\n");
            sb.append("**").append(team2Name).append("** (avg: ").append(team2AvgSR).append(")\n");
        }

        // Role balance
        sb.append("\n**Role Balance:**\n");
        sb.append(formatRoleDiff("Tank", tankSRDiff, 500));
        sb.append(formatRoleDiff("DPS", dpsSRDiff, 300));
        sb.append(formatRoleDiff("Support", supportSRDiff, 300));

        // Queue
        if (queue != null && !queue.isEmpty()) {
            sb.append("\n**📋 Queue (").append(queue.size()).append("):**\n");
            for (Player p : queue) {
                String priority = p.getGamesSkipped() > 0 ? " 🔺" : "";
                sb.append("• ").append(p).append(priority).append("\n");
            }
        }

        return sb.toString();
    }

    private String formatRoleDiff(String role, int diff, int threshold) {
        String status = diff <= threshold / 2 ? "✅" : diff <= threshold ? "⚠️" : "❌";
        return status + " " + role + ": " + diff + " SR\n";
    }

}
