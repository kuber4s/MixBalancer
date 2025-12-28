package com.overwatch.balancer.service;

import com.overwatch.balancer.core.BalanceStrategy;
import com.overwatch.balancer.domain.enumeration.GameMap;
import com.overwatch.balancer.domain.enumeration.GameMode;
import com.overwatch.balancer.domain.model.Match;
import com.overwatch.balancer.domain.model.Player;
import com.overwatch.balancer.exception.BalanceException;
import com.overwatch.balancer.repository.MatchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BalancerService {

    private static final int PLAYERS_PER_MATCH = 10;
    private static final int MAX_CONCURRENT_MATCHES = 5;

    private final BalanceStrategy balanceStrategy;
    private final LobbyService lobbyService;
    private final PlayerService playerService;
    private final MatchRepository matchRepository;

    public Match balance(String lobbyId) {
        return balanceMultiple(lobbyId, 1).getFirst();
    }

    public List<Match> balanceMultiple(String lobbyId, int matchCount) {
        // validate
        if (matchCount < 1) {
            throw new BalanceException("Match count must be at least 1");
        }
        matchCount = Math.min(matchCount, MAX_CONCURRENT_MATCHES);

        List<Player> allPlayers = lobbyService.getPlayers(lobbyId);

        if (allPlayers.size() < PLAYERS_PER_MATCH) {
            int needed = PLAYERS_PER_MATCH - allPlayers.size();
            throw new BalanceException("Need " + needed + " more players to start");
        }

        // limit by available players
        int maxPossible = allPlayers.size() / PLAYERS_PER_MATCH;
        matchCount = Math.min(matchCount, maxPossible);

        List<Match> matches = new ArrayList<>();
        List<Player> remaining = new ArrayList<>(allPlayers);

        for (int i = 0; i < matchCount; i++) {
            if (remaining.size() < PLAYERS_PER_MATCH) {
                break;
            }

            Optional<BalanceStrategy.BalanceResult> result = balanceStrategy.balance(remaining);

            if (result.isEmpty()) {
                log.warn("Failed to balance match {} of {}", i + 1, matchCount);
                break;
            }

            BalanceStrategy.BalanceResult balance = result.get();

            // create
            GameMap map = GameMap.random();
            Match match = Match.create(balance.team1(), balance.team2(), map, balance.queue());
            match.setLobbyId(lobbyId);

            matchRepository.save(match);
            matches.add(match);

            // update stats for playing players
            List<Player> playing = new ArrayList<>();
            playing.addAll(balance.team1().getPlayers());
            playing.addAll(balance.team2().getPlayers());
            playerService.recordGamesPlayed(playing);

            remaining.removeAll(playing);

            log.info("Created match {}/{}: {} vs {} on {}",
                    i + 1, matchCount,
                    balance.team1().getName(),
                    balance.team2().getName(),
                    map.getDisplayName());
        }

        if (matches.isEmpty()) {
            throw new BalanceException("Failed to create any matches");
        }

        // Update stats for waiting players
        if (!remaining.isEmpty()) {
            playerService.recordGamesSkipped(remaining);
        }

        return matches;
    }

    public Match balancePlayers(List<Player> players, GameMode mapMode) {
        if (players.size() < PLAYERS_PER_MATCH) {
            throw new BalanceException("Need at least " + PLAYERS_PER_MATCH + " players");
        }

        Optional<BalanceStrategy.BalanceResult> result = balanceStrategy.balance(players);

        if (result.isEmpty()) {
            throw new BalanceException("Failed to balance teams. Check role distribution.");
        }

        BalanceStrategy.BalanceResult balance = result.get();

        // Select map
        GameMap map = mapMode != null
                ? GameMap.randomByMode(mapMode)
                : GameMap.random();

        Match match = Match.create(balance.team1(), balance.team2(), map, balance.queue());
        matchRepository.save(match);

        return match;
    }

    public LobbyService.QueueStatus getQueue(String lobbyId) {
        return lobbyService.getQueueStatus(lobbyId);
    }

    public Optional<Match> getMatch(String matchId) {
        return matchRepository.findById(matchId);
    }

    public List<Match> getRecentMatches(int limit) {
        return matchRepository.findRecent(limit);
    }

    public List<Match> getMatchesForLobby(String lobbyId) {
        return matchRepository.findByLobbyId(lobbyId);
    }

    public List<GameMap> getAllMaps() {
        return Arrays.asList(GameMap.values());
    }

    public List<GameMap> getMapsByMode(GameMode mode) {
        return GameMap.getByMode(mode);
    }

}
