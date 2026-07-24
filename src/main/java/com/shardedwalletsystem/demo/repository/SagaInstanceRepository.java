package com.shardedwalletsystem.demo.repository;

import com.shardedwalletsystem.demo.model.SagaInstance;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SagaInstanceRepository extends JpaRepository<SagaInstance, Long> {
}
