package com.shardedwalletsystem.demo.repository;

import com.shardedwalletsystem.demo.model.SagaStep;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SagaStepRepository extends JpaRepository<SagaStep, Long> {
}
