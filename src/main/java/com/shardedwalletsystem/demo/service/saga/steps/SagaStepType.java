package com.shardedwalletsystem.demo.service.saga.steps;

public enum SagaStepType {
    DEBIT_SOURCE_WALLET_STEP,
    CREDIT_DESTINATION_WALLET_STEP,
    UPDATE_TRANSACTION_STATUS_STEP,
}
