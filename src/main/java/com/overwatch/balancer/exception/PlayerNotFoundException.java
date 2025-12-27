package com.overwatch.balancer.exception;

public class PlayerNotFoundException extends RuntimeException {
    public PlayerNotFoundException(String id) {
        super("Player not found: " + id);
    }
}
