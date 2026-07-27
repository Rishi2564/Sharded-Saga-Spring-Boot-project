package com.shardedwalletsystem.demo.repository;

import com.shardedwalletsystem.demo.model.SagaStep;
import com.shardedwalletsystem.demo.model.StepStatus;
import com.shardedwalletsystem.demo.service.saga.steps.SagaStepType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SagaStepRepository extends JpaRepository<SagaStep, Long> {
    Optional<SagaStep> findBySagaInstanceIdAndStepNameAndStatus(Long sagaInstanceId,String stepName, StepStatus status);
}
