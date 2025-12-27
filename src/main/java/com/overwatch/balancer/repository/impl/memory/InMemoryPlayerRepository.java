package com.overwatch.balancer.repository.impl.memory;

import com.overwatch.balancer.domain.model.Player;
import com.overwatch.balancer.repository.PlayerRepository;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class InMemoryPlayerRepository implements PlayerRepository {

    private final Map<String, Player> storage = new ConcurrentHashMap<>();

    @Override
    public Player save(Player player) {
        storage.put(player.getId(), player);
        log.info("[Tech][INFO] Saved player: {} ({})", player.getName(), player.getId());
        return player;
    }

    @Override
    public Optional<Player> findById(String id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public boolean existsById(String id) {
        return storage.containsKey(id);
    }

    @Override
    public void deleteById(String id) {
        Player removed = storage.remove(id);
        if (removed != null) {
            log.info("[Tech][INFO] Deleted player: {} ({})", removed.getName(), id);
        }
    }

    @Override
    public List<Player> findAllById(Collection<String> ids) {
        return ids.stream()
                .map(storage::get)
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public List<Player> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public long count() {
        return storage.size();
    }

    @Override
    public List<Player> saveAll(Collection<Player> players) {
        players.forEach(this::save);
        return new ArrayList<>(players);
    }

}
