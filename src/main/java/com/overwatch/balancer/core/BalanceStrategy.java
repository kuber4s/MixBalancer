package com.overwatch.balancer.core;

import com.overwatch.balancer.domain.model.Player;
import com.overwatch.balancer.domain.model.Team;

import java.util.List;
import java.util.Optional;

public interface BalanceStrategy {

    Optional<BalanceResult> balance(List<Player> players);
    String getName();

    record BalanceResult(
            Team team1,
            Team team2,
            List<Player> queue,
            double score,
            BalanceMetrics metrics
    ) {
        public boolean isValid() {
            return team1.isFull() && team2.isFull();
        }

        public int getSRDifference() {
            return Math.abs((int) team1.getAverageSR() - (int) team2.getAverageSR());
        }
    }

    /**
     * Detailed metrics for balance quality assessment.
     */
    record BalanceMetrics(
            int overallSRDiff,
            int tankSRDiff,
            int dpsSRDiff,
            int supportSRDiff,
            int maxRoleDiff,
            double balanceScore
    ) {
        public static BalanceMetrics calculate(Team team1, Team team2) {
            int overallDiff = Math.abs((int) team1.getAverageSR() - (int) team2.getAverageSR());
            int tankDiff = Math.abs(team1.getTankSR() - team2.getTankSR());
            int dpsDiff = Math.abs(
                    (int) team1.getRoleAverageSR(com.overwatch.balancer.domain.enumeration.Role.DPS) -
                            (int) team2.getRoleAverageSR(com.overwatch.balancer.domain.enumeration.Role.DPS)
            );
            int supportDiff = Math.abs(
                    (int) team1.getRoleAverageSR(com.overwatch.balancer.domain.enumeration.Role.SUPPORT) -
                            (int) team2.getRoleAverageSR(com.overwatch.balancer.domain.enumeration.Role.SUPPORT)
            );
            int maxRoleDiff = Math.max(tankDiff, Math.max(dpsDiff, supportDiff));

            // lower score = better balance
            // !!! tank difference has 2x weight because there's only 1 tank !!!
            double score = overallDiff * 1.5 + tankDiff * 2.0 + dpsDiff + supportDiff;

            // Penalty for extreme tank difference
            if (tankDiff > 500) {
                score += (tankDiff - 500) * 3;
            }

            return new BalanceMetrics(overallDiff, tankDiff, dpsDiff, supportDiff, maxRoleDiff, score);
        }
    }

}
