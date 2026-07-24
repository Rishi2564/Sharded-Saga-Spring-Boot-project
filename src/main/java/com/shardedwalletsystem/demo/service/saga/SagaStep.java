package com.shardedwalletsystem.demo.service.saga;

public interface SagaStep {
    boolean execute(SagaContext sagaContext);

    boolean compensate(SagaContext sagaContext);

    String getStepName();
}
