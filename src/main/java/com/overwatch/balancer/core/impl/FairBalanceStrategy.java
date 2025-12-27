package com.overwatch.balancer.core.impl;

import com.overwatch.balancer.core.BalanceStrategy;
import com.overwatch.balancer.domain.enumeration.Role;
import com.overwatch.balancer.domain.model.Player;
import com.overwatch.balancer.domain.model.Team;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Slf4j
public class FairBalanceStrategy implements BalanceStrategy {

    private static final int DEFAULT_ITERATIONS = 3000;
    private static final int PLAYERS_PER_MATCH = 10;

    private final int maxIterations;

    public FairBalanceStrategy() {
        this(DEFAULT_ITERATIONS);
    }

    public FairBalanceStrategy(int maxIterations) {
        this.maxIterations = maxIterations;
    }

    @Override
    public String getName() {
        return "FairBalance";
    }

    @Override
    public Optional<BalanceResult> balance(List<Player> players) {
        if (players.size() < PLAYERS_PER_MATCH) {
            log.warn("Not enough players: {} < {}", players.size(), PLAYERS_PER_MATCH);
            return Optional.empty();
        }

        // validate role availability
        ValidationResult validation = validateRoles(players);
        if (!validation.isValid()) {
            log.warn("[Tech][WARN] Role validation failed: {}", validation.message());
            return Optional.empty();
        }

        // sort by priority
        List<Player> prioritized = prioritizePlayers(players);

        // select candidates for this match
        List<Player> candidates = selectCandidates(prioritized);

        // find best balance
        BalanceResult best = null;
        double bestScore = Double.MAX_VALUE;

        for (int i = 0; i < maxIterations; i++) {
            Optional<BalanceResult> attempt = tryBalance(candidates, i);

            if (attempt.isPresent()) {
                BalanceResult result = attempt.get();
                double score = result.metrics().balanceScore();

                if (score < bestScore) {
                    bestScore = score;
                    best = result;

                    // early exit on excellent balance
                    if (score < 150 && result.metrics().tankSRDiff() < 200) {
                        log.debug("Found excellent balance at iteration {}", i);
                        break;
                    }
                }
            }
        }

        if (best == null) {
            log.warn("[Tech][WARN] Failed to find valid balance after {} iterations", maxIterations);
            return Optional.empty();
        }

        // Create queue from remaining players
        Set<Player> playing = new HashSet<>(best.team1().getPlayers());
        playing.addAll(best.team2().getPlayers());

        List<Player> queue = players.stream()
                .filter(p -> !playing.contains(p))
                .sorted(Comparator.comparingInt(Player::getPriorityScore).reversed())
                .toList();

        log.info("[Tech][INFO] Balance complete: SR diff={}, tank diff={}, quality={}",
                best.metrics().overallSRDiff(),
                best.metrics().tankSRDiff(),
                getQualityLabel(best.metrics().balanceScore()));

        return Optional.of(new BalanceResult(
                best.team1(),
                best.team2(),
                queue,
                best.score(),
                best.metrics()
        ));
    }

    /**
     * validates that enough players exist for each role
     */
    private ValidationResult validateRoles(List<Player> players) {
        for (Role role : Role.values()) {
            long available = players.stream().filter(p -> p.canPlay(role)).count();
            int required = role.getSlotsPerMatch();

            if (available < required) {
                return new ValidationResult(false,
                        String.format("Not enough %s players: %d < %d", role.getName(), available, required));
            }
        }
        return new ValidationResult(true, "OK");
    }

    private List<Player> prioritizePlayers(List<Player> players) {
        return players.stream()
                .sorted(Comparator
                        .comparingInt(Player::getGamesSkipped).reversed()
                        .thenComparingInt(Player::getHighestSR).reversed())
                .collect(Collectors.toList());
    }

    private List<Player> selectCandidates(List<Player> prioritized) {
        Set<Player> selected = new LinkedHashSet<>();
        Map<Role, Integer> needed = new EnumMap<>(Role.class);

        for (Role role : Role.values()) {
            needed.put(role, role.getSlotsPerMatch());
        }

        // first pass: select by primary role
        for (Player player : prioritized) {
            if (selected.size() >= PLAYERS_PER_MATCH) break;

            Role primary = player.getPrimaryRole();
            if (primary != null && needed.get(primary) > 0) {
                selected.add(player);
                needed.merge(primary, -1, Integer::sum);
            }
        }

        // Second pass: fill remaining with flex players
        for (Player player : prioritized) {
            if (selected.size() >= PLAYERS_PER_MATCH) break;
            if (selected.contains(player)) continue;

            for (Role role : Role.values()) {
                if (needed.get(role) > 0 && player.canPlay(role)) {
                    selected.add(player);
                    needed.merge(role, -1, Integer::sum);
                    break;
                }
            }
        }

        // Third pass: just add remaining players if needed
        for (Player player : prioritized) {
            if (selected.size() >= PLAYERS_PER_MATCH) break;
            selected.add(player);
        }

        return new ArrayList<>(selected);
    }

    /**
     * Attempts to create balanced teams.
     */
    private Optional<BalanceResult> tryBalance(List<Player> candidates, int iteration) {
        Random random = ThreadLocalRandom.current();

        // Create fresh teams
        Team team1 = new Team("🔴 Red Team", "🔴");
        Team team2 = new Team("🔵 Blue Team", "🔵");

        Set<Player> assigned = new HashSet<>();

        // Phase 1: pair tanks by closest SR (critical!)
        boolean tanksAssigned = assignTanks(candidates, team1, team2, assigned, random, iteration);
        if (!tanksAssigned) {
            return Optional.empty();
        }

        // phase 2: assign DPS with balance consideration
        boolean dpsAssigned = assignRole(Role.DPS, candidates, team1, team2, assigned, random);
        if (!dpsAssigned) {
            return Optional.empty();
        }

        // phase 3: assign Support with balance consideration
        boolean supportAssigned = assignRole(Role.SUPPORT, candidates, team1, team2, assigned, random);
        if (!supportAssigned) {
            return Optional.empty();
        }

        if (!team1.isFull() || !team2.isFull()) {
            return Optional.empty();
        }

        BalanceMetrics metrics = BalanceMetrics.calculate(team1, team2);

        return Optional.of(new BalanceResult(
                team1,
                team2,
                List.of(),
                metrics.balanceScore(),
                metrics
        ));
    }

    private boolean assignTanks(List<Player> candidates, Team team1, Team team2,
                                Set<Player> assigned, Random random, int iteration) {
        List<Player> tanks = candidates.stream()
                .filter(p -> p.canPlay(Role.TANK))
                .sorted(Comparator.comparingInt(p -> -p.getRating(Role.TANK)))
                .collect(Collectors.toList());

        if (tanks.size() < 2) {
            return false;
        }

        // Strategy: find the closest pair of tanks
        Player tank1 = null;
        Player tank2 = null;
        int minDiff = Integer.MAX_VALUE;

        // On first iterations, use optimal pairing
        // On later iterations, add randomness for diversity
        if (iteration < maxIterations / 2) {
            // find closest pair
            for (int i = 0; i < tanks.size(); i++) {
                for (int j = i + 1; j < tanks.size(); j++) {
                    int diff = Math.abs(tanks.get(i).getRating(Role.TANK) -
                            tanks.get(j).getRating(Role.TANK));

                    // Prefer pairs with high priority players
                    int priorityBonus = tanks.get(i).getGamesSkipped() + tanks.get(j).getGamesSkipped();
                    int adjustedDiff = diff - priorityBonus * 50;

                    if (adjustedDiff < minDiff) {
                        minDiff = adjustedDiff;
                        tank1 = tanks.get(i);
                        tank2 = tanks.get(j);
                    }
                }
            }
        } else {
            // add randomness on later iterations
            Collections.shuffle(tanks, random);
            tank1 = tanks.get(0);
            tank2 = tanks.get(1);
        }

        if (tank1 == null || tank2 == null) {
            return false;
        }

        team1.addPlayer(tank1, Role.TANK);
        team2.addPlayer(tank2, Role.TANK);
        assigned.add(tank1);
        assigned.add(tank2);

        return true;
    }

    /**
     * Assigns players for a role with balance consideration
     */
    private boolean assignRole(Role role, List<Player> candidates, Team team1, Team team2,
                               Set<Player> assigned, Random random) {
        List<Player> available = candidates.stream()
                .filter(p -> !assigned.contains(p) && p.canPlay(role))
                .sorted(Comparator.comparingInt((Player p) -> -p.getRating(role)))
                .collect(Collectors.toList());

        int slotsPerTeam = role.getSlotsPerTeam();
        int needed = slotsPerTeam * 2;

        if (available.size() < needed) {
            return false;
        }

        // greedy assignment: always assign to team with lower total SR
        List<Player> toAssign = new ArrayList<>(available.subList(0, needed));

        // add some randomness in order
        if (random.nextDouble() < 0.3) {
            Collections.shuffle(toAssign, random);
        }

        for (Player player : toAssign) {
            if (assigned.contains(player)) continue;

            // assign to team with lower SR for this role (or lower total SR)
            double team1RoleSR = team1.getRoleAverageSR(role);
            double team2RoleSR = team2.getRoleAverageSR(role);

            boolean toTeam1;
            if (team1.getFreeSlots(role) == 0) {
                toTeam1 = false;
            } else if (team2.getFreeSlots(role) == 0) {
                toTeam1 = true;
            } else {
                // Consider both role SR and total SR
                double team1Total = team1.getTotalSR();
                double team2Total = team2.getTotalSR();

                // Weighted decision
                double team1Score = team1RoleSR * 0.4 + team1Total * 0.6;
                double team2Score = team2RoleSR * 0.4 + team2Total * 0.6;

                toTeam1 = team1Score <= team2Score;
            }

            Team target = toTeam1 ? team1 : team2;
            if (target.getFreeSlots(role) == 0) {
                target = toTeam1 ? team2 : team1;
            }

            if (target.addPlayer(player, role)) {
                assigned.add(player);
            }
        }

        return team1.getFreeSlots(role) == 0 && team2.getFreeSlots(role) == 0;
    }

    private String getQualityLabel(double score) {
        if (score < 200) return "EXCELLENT";
        if (score < 400) return "GOOD";
        if (score < 700) return "FAIR";
        return "POOR";
    }

    private record ValidationResult(boolean isValid, String message) {}

}
