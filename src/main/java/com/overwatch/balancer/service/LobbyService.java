package com.overwatch.balancer.service;

import com.overwatch.balancer.domain.enumeration.Role;
import com.overwatch.balancer.domain.model.Lobby;
import com.overwatch.balancer.domain.model.Player;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class LobbyService {

    private static final int MIN_PLAYERS_FOR_MATCH = 10;

    private final PlayerService playerService;

    private final Map<String, Set<String>> lobbies = new ConcurrentHashMap<>();

    public Lobby getOrCreateLobby(String lobbyId) {
        lobbies.computeIfAbsent(lobbyId, k -> ConcurrentHashMap.newKeySet());
        return new Lobby(lobbyId, this);
    }

    public boolean join(String lobbyId, String playerId) {
        Player player = playerService.getPlayerOrThrow(playerId);

        if (!player.hasActiveRole()) {
            throw new IllegalStateException("Player has no active roles");
        }

        Set<String> lobby = lobbies.computeIfAbsent(lobbyId, k -> ConcurrentHashMap.newKeySet());
        boolean added = lobby.add(playerId);

        if (added) {
            log.debug("Player {} joined lobby {}", player.getName(), lobbyId);
        }

        return added;
    }

    public boolean leave(String lobbyId, String playerId) {
        Set<String> lobby = lobbies.get(lobbyId);

        if (lobby == null) {
            return false;
        }

        boolean removed = lobby.remove(playerId);

        if (removed) {
            log.debug("Player {} left lobby {}", playerId, lobbyId);
        }

        return removed;
    }

    public boolean isInLobby(String lobbyId, String playerId) {
        Set<String> lobby = lobbies.get(lobbyId);
        return lobby != null && lobby.contains(playerId);
    }

    public List<Player> getPlayers(String lobbyId) {
        Set<String> lobby = lobbies.get(lobbyId);

        if (lobby == null || lobby.isEmpty()) {
            return List.of();
        }

        return playerService.getPlayers(lobby);
    }

    public Set<String> getPlayerIds(String lobbyId) {
        Set<String> lobby = lobbies.get(lobbyId);
        return lobby != null ? Set.copyOf(lobby) : Set.of();
    }

    public int getPlayerCount(String lobbyId) {
        Set<String> lobby = lobbies.get(lobbyId);
        return lobby != null ? lobby.size() : 0;
    }

    public boolean canStartMatch(String lobbyId) {
        return getPlayerCount(lobbyId) >= MIN_PLAYERS_FOR_MATCH;
    }

    public int getMaxMatches(String lobbyId) {
        return getPlayerCount(lobbyId) / MIN_PLAYERS_FOR_MATCH;
    }

    public void removePlayers(String lobbyId, Collection<String> playerIds) {
        Set<String> lobby = lobbies.get(lobbyId);
        if (lobby != null) {
            lobby.removeAll(playerIds);
        }
    }

    public void clearLobby(String lobbyId) {
        lobbies.remove(lobbyId);
        log.info("Cleared lobby {}", lobbyId);
    }

    public QueueStatus getQueueStatus(String lobbyId) {
        List<Player> players = getPlayers(lobbyId);

        // sort by priority
        List<Player> sorted = players.stream()
                .sorted(Comparator
                        .comparingInt(Player::getGamesSkipped).reversed()
                        .thenComparingInt(Player::getHighestSR).reversed())
                .toList();

        int total = sorted.size();
        int forMatch = Math.min(total, MIN_PLAYERS_FOR_MATCH);
        int inQueue = Math.max(0, total - MIN_PLAYERS_FOR_MATCH);

        List<Player> playing = sorted.subList(0, forMatch);
        List<Player> waiting = inQueue > 0 ? sorted.subList(forMatch, total) : List.of();

        // check role availability
        Map<Role, Long> roleCount = new EnumMap<>(Role.class);
        for (Role role : Role.values()) {
            roleCount.put(role, players.stream().filter(p -> p.canPlay(role)).count());
        }

        return new QueueStatus(
                lobbyId,
                total,
                playing,
                waiting,
                canStartMatch(lobbyId),
                roleCount
        );
    }

    public String formatPlayerList(String lobbyId) {
        List<Player> players = getPlayers(lobbyId);

        if (players.isEmpty()) {
            return "No players in lobby";
        }

        // group by primary role
        Map<Role, List<Player>> byRole = new EnumMap<>(Role.class);
        for (Role role : Role.values()) {
            byRole.put(role, new ArrayList<>());
        }

        for (Player player : players) {
            Role primary = player.getPrimaryRole();
            if (primary != null) {
                byRole.get(primary).add(player);
            }
        }

        StringBuilder sb = new StringBuilder();

        for (Role role : Role.values()) {
            List<Player> rolePlayers = byRole.get(role);
            if (!rolePlayers.isEmpty()) {
                // Sort by SR descending
                rolePlayers.sort((a, b) -> Integer.compare(b.getRating(role), a.getRating(role)));

                sb.append(role.getEmoji()).append(" **").append(role.getName());
                sb.append("** (").append(rolePlayers.size()).append("):\n");

                for (Player p : rolePlayers) {
                    sb.append("  ").append(p.getName());
                    sb.append(" — ").append(p.getRating(role));
                    if (p.getGamesSkipped() > 0) {
                        sb.append(" 🔺");
                    }
                    sb.append("\n");
                }
            }
        }

        return sb.toString();
    }

    public record QueueStatus(
            String lobbyId,
            int totalPlayers,
            List<Player> playingNext,
            List<Player> waiting,
            boolean canStart,
            Map<Role, Long> roleAvailability
    ) {
        public int getWaitingCount() {
            return waiting.size();
        }

        public int getPlayersNeeded() {
            return Math.max(0, MIN_PLAYERS_FOR_MATCH - totalPlayers);
        }
    }

}
