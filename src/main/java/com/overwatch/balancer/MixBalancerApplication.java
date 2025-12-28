package com.overwatch.balancer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Team balancing engine for Overwatch 2.
 *
 * <h2>Profiles:</h2>
 * <ul>
 *   <li>{@code memory} - in-memory storage (default)</li>
 *   <li>{@code postgres} - PostgreSQL database</li>
 * </ul>
 *
 * @author kuber4s
 * @version 1.0
 */
@Slf4j
@SpringBootApplication
@EntityScan("com.overwatch.balancer.domain.model")
@EnableJpaRepositories("com.overwatch.balancer.repository.impl.jpa")
public class MixBalancerApplication {

	public static void main(String[] args) {
		printBanner();
		SpringApplication.run(MixBalancerApplication.class, args);
	}

	private static void printBanner() {
		log.info("""
                
                ╔═══════════════════════════════════════════════════════╗
                ║                                                       ║
                ║         Team balancing engine for Overwatch 2         ║
                ║                          v1.0                         ║
                ║                                                       ║
                ╚═══════════════════════════════════════════════════════╝
                """);
	}

}
