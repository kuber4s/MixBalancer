package com.overwatch.balancer.config.db;

import com.overwatch.balancer.repository.MatchRepository;
import com.overwatch.balancer.repository.PlayerRepository;
import com.overwatch.balancer.repository.impl.memory.InMemoryMatchRepository;
import com.overwatch.balancer.repository.impl.memory.InMemoryPlayerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Slf4j
@Configuration
@Profile("memory")
public class InMemoryConfig {

    @Bean
    @Primary
    public PlayerRepository playerRepository() {
        log.info("[Tech][INFO] Using in-memory PlayerRepository");
        return new InMemoryPlayerRepository();
    }

    @Bean
    @Primary
    public MatchRepository matchRepository() {
        log.info("[Tech][INFO] Using in-memory MatchRepository");
        return new InMemoryMatchRepository();
    }

}
