package com.shardedwalletsystem.demo.service.saga.steps;

import com.shardedwalletsystem.demo.model.Wallet;
import com.shardedwalletsystem.demo.repository.WalletRepository;
import com.shardedwalletsystem.demo.service.saga.SagaContext;
import com.shardedwalletsystem.demo.service.saga.SagaStep;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class DebitSourceWalletStep implements SagaStep {
    private final WalletRepository walletRepository;

    @Override
    @Transactional
    public boolean execute(SagaContext context) {
        Long fromWalletId=context.getLong("fromWalletId");
        BigDecimal amount=context.getBigDecimal("amount");
        log.info("Debiting source wallet {} with amount {}",fromWalletId,amount);
        Wallet wallet=walletRepository.findByIdWithLock(fromWalletId).orElseThrow(()->new RuntimeException("Wallet not found"));
        log.info("Wallet fetched with balance {}",wallet.getBalance());
        context.put("originalSourceWalletBalance",wallet.getBalance());
        wallet.debit(amount);
        walletRepository.save(wallet);
        log.info("Wallet saved with balance {}",wallet.getBalance());
        context.put("sourceWalletBalanceAfterDebit",wallet.getBalance());
        log.info("Debit source wallet step executed successfully");
        return true;
    }

    @Override
    @Transactional
    public boolean compensate(SagaContext context) {
        Long fromWalletId=context.getLong("fromWalletId");
        BigDecimal amount=context.getBigDecimal("amount");
        log.info("Compensating source wallet {} with amount {}", fromWalletId,amount);
        Wallet wallet=walletRepository.findByIdWithLock(fromWalletId).orElseThrow(()->new RuntimeException("Wallet not found"));
        log.info("Wallet fetched with balance {}",wallet.getBalance());
        context.put("sourceWalletBalanceBeforeCreditCompensation",wallet.getBalance());
        wallet.credit(amount);
        walletRepository.save(wallet);
        log.info("Wallet saved with balance {}",wallet.getBalance());
        context.put("sourceWalletBalanceAfterCreditCompensation",wallet.getBalance());
        log.info("Compensating source wallet step executed successfully");
        return true;
    }

    @Override
    public String getStepName() {
        return "DebitSourceWalletStep";
    }
}
