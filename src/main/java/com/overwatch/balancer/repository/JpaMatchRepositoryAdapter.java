package com.overwatch.balancer.repository;

import com.overwatch.balancer.domain.model.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JpaMatchRepositoryAdapter extends JpaRepository<Match, String> {

    List<Match> findAllByOrderByCreatedAtDesc();

    List<Match> findByLobbyIdOrderByCreatedAtDesc(String lobbyId);

    @Query("SELECT m FROM Match m ORDER BY m.createdAt DESC LIMIT :limit")
    List<Match> findRecent(@Param("limit") int limit);

}
