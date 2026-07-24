package com.shardedwalletsystem.demo.repository;

import com.shardedwalletsystem.demo.model.SagaInstance;
import com.shardedwalletsystem.demo.service.saga.SagaContext;

public interface SagaOrchestrator {
    Long startSaga(SagaContext context);

    boolean executeStep(Long sagaInstanceId, String stepName);

    boolean compensateStep(Long sagaInstanceId, String stepName);

    SagaInstance getSagaInstance(Long sagaInstanceId);

    void compensateSaga(Long sagaInstanceId);

    void failSaga(Long sagaInstanceId);

    void completeSaga(Long sagaInstanceId);
}
