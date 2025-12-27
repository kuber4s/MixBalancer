package com.overwatch.balancer.config.db;

import com.overwatch.balancer.repository.JpaMatchRepositoryAdapter;
import com.overwatch.balancer.repository.JpaPlayerRepositoryAdapter;
import com.overwatch.balancer.repository.MatchRepository;
import com.overwatch.balancer.repository.PlayerRepository;
import com.overwatch.balancer.repository.impl.jpa.JpaMatchRepository;
import com.overwatch.balancer.repository.impl.jpa.JpaPlayerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Slf4j
@Configuration
@Profile("postgres")
public class JpaConfig {

    @Bean
    @Primary
    public PlayerRepository playerRepository(JpaPlayerRepositoryAdapter jpaRepository) {
        log.info("[Tech][INFO] Using JPA PlayerRepository");
        return new JpaPlayerRepository(jpaRepository);
    }

    @Bean
    @Primary
    public MatchRepository matchRepository(JpaMatchRepositoryAdapter jpaRepository) {
        log.info("[Tech][INFO] Using JPA MatchRepository");
        return new JpaMatchRepository(jpaRepository);
    }

}
