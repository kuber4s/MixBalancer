package com.overwatch.balancer.repository;

import com.overwatch.balancer.domain.model.Match;

import java.util.List;
import java.util.Optional;

public interface MatchRepository {

    Match save(Match match);
    Optional<Match> findById(String id);
    List<Match> findAllOrderByCreatedAtDesc();
    List<Match> findByLobbyId(String lobbyId);
    List<Match> findRecent(int limit);
    long count();

}
