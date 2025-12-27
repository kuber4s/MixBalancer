package com.overwatch.balancer.repository.impl.jpa;

import com.overwatch.balancer.domain.model.Match;
import com.overwatch.balancer.repository.JpaMatchRepositoryAdapter;
import com.overwatch.balancer.repository.MatchRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class JpaMatchRepository implements MatchRepository {

    private final JpaMatchRepositoryAdapter jpaRepository;

    @Override
    public Match save(Match match) {
        return jpaRepository.save(match);
    }

    @Override
    public Optional<Match> findById(String id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<Match> findAllOrderByCreatedAtDesc() {
        return jpaRepository.findAllByOrderByCreatedAtDesc();
    }

    @Override
    public List<Match> findByLobbyId(String lobbyId) {
        return jpaRepository.findByLobbyIdOrderByCreatedAtDesc(lobbyId);
    }

    @Override
    public List<Match> findRecent(int limit) {
        return jpaRepository.findRecent(limit);
    }

    @Override
    public long count() {
        return jpaRepository.count();
    }

}
