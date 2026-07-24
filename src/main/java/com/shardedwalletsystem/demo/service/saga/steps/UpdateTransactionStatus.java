package com.shardedwalletsystem.demo.service.saga.steps;

import com.shardedwalletsystem.demo.model.Transaction;
import com.shardedwalletsystem.demo.model.TransactionStatus;
import com.shardedwalletsystem.demo.repository.TransactionRepository;
import com.shardedwalletsystem.demo.service.saga.SagaContext;
import com.shardedwalletsystem.demo.service.saga.SagaStep;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UpdateTransactionStatus implements SagaStep {
    private final TransactionRepository transactionRepository;

    @Override
    public boolean execute(SagaContext context) {
        Long transactionId=context.getLong("transactionId");
        log.info("Updating transaction status for transaction {}",transactionId);
        Transaction transaction=transactionRepository.findById(transactionId).orElseThrow(()->new RuntimeException("Transaction not found"));
        context.put("originalTransactionStatus",transaction.getStatus());
        transaction.setStatus(TransactionStatus.SUCCESS);
        transactionRepository.save(transaction);
        log.info("Transaction status updated for transaction {}",transactionId);
        context.put("transactionStatusAfterUpdate",transaction.getStatus());
        log.info("Update transaction status step executed successfully");
        return true;
    }
    @Override
    public boolean compensate(SagaContext context) {
        return false;
    }
    @Override
    public String getStepName(){
        return "UpdateTransactionStatus";
    }
}
