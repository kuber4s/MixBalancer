package com.overwatch.balancer.config;

import com.overwatch.balancer.core.BalanceStrategy;
import com.overwatch.balancer.core.impl.FairBalanceStrategy;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class BalancerConfig {

    @Value("${balancer.max-iterations:3000}")
    private int maxIterations;

    @Bean
    public BalanceStrategy balanceStrategy() {
        log.info("[Tech][INFO] Initializing FairBalanceStrategy with {} iterations", maxIterations);
        return new FairBalanceStrategy(maxIterations);
    }

}
