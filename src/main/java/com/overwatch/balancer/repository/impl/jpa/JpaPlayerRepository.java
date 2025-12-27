package com.overwatch.balancer.repository.impl.jpa;

import com.overwatch.balancer.domain.model.Player;
import com.overwatch.balancer.repository.JpaPlayerRepositoryAdapter;
import com.overwatch.balancer.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class JpaPlayerRepository implements PlayerRepository {

    private final JpaPlayerRepositoryAdapter jpaRepository;

    @Override
    public Player save(Player player) {
        return jpaRepository.save(player);
    }

    @Override
    public Optional<Player> findById(String id) {
        return jpaRepository.findById(id);
    }

    @Override
    public boolean existsById(String id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public void deleteById(String id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public List<Player> findAllById(Collection<String> ids) {
        return jpaRepository.findAllById(ids);
    }

    @Override
    public List<Player> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public long count() {
        return jpaRepository.count();
    }

    @Override
    public List<Player> saveAll(Collection<Player> players) {
        return jpaRepository.saveAll(players);
    }

}
