package com.overwatch.balancer.repository.impl.memory;

import com.overwatch.balancer.domain.model.Match;
import com.overwatch.balancer.repository.MatchRepository;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
public class InMemoryMatchRepository implements MatchRepository {

    private final Map<String, Match> storage = new ConcurrentHashMap<>();

    @Override
    public Match save(Match match) {
        storage.put(match.getId(), match);
        log.debug("Saved match: {}", match.getId());
        return match;
    }

    @Override
    public Optional<Match> findById(String id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Match> findAllOrderByCreatedAtDesc() {
        return storage.values().stream()
                .sorted(Comparator.comparing(Match::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public List<Match> findByLobbyId(String lobbyId) {
        return storage.values().stream()
                .filter(m -> lobbyId.equals(m.getLobbyId()))
                .sorted(Comparator.comparing(Match::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public List<Match> findRecent(int limit) {
        return storage.values().stream()
                .sorted(Comparator.comparing(Match::getCreatedAt).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    public long count() {
        return storage.size();
    }

}
