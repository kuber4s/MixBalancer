package com.overwatch.balancer.repository;

import com.overwatch.balancer.domain.model.Player;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaPlayerRepositoryAdapter extends JpaRepository<Player, String> {
}
