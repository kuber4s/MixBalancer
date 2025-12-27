package com.overwatch.balancer.repository;

import com.overwatch.balancer.domain.model.Player;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PlayerRepository {

    Player save(Player player);
    Optional<Player> findById(String id);
    boolean existsById(String id);
    void deleteById(String id);
    List<Player> findAllById(Collection<String> ids);
    List<Player> findAll();
    long count();
    List<Player> saveAll(Collection<Player> players);

}
