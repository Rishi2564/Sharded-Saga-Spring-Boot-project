package com.shardedwalletsystem.demo.model;

public enum SagaStatus {
    STARTED,
    RUNNING,
    COMPLETED,
    FAILED,
    COMPENSATING,
    COMPENSATED,
}
