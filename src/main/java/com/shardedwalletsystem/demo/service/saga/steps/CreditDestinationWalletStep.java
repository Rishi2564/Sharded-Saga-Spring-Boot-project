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
@Slf4j
@RequiredArgsConstructor
public class CreditDestinationWalletStep implements SagaStep {
    private final WalletRepository walletRepository;
    @Override
    @Transactional
    public boolean execute(SagaContext context){
        Long toWalletId=context.getLong("toWalletId");
        BigDecimal amount=context.getBigDecimal("amount");
        log.info("Crediting destination wallet {} with amount {}",toWalletId,amount);

        Wallet wallet= walletRepository.findByIdWithLock(toWalletId).orElseThrow(()->new RuntimeException("no wallet found"));
        log.info("Wallet fetched with balance {}",wallet.getBalance());
        context.put("toWalletBalanceBeforeCredit {}",wallet.getBalance());
        wallet.credit(amount);

        walletRepository.save(wallet);
        log.info("wallet saved with balance {}",wallet.getBalance());
        context.put("toWalletBalanceAfterCredit",wallet.getBalance());
        return true;
    }

    @Override
    @Transactional
    public boolean compensate(SagaContext context){
        Long toWalletId=context.getLong("toWalletId");
        BigDecimal amount=context.getBigDecimal("amount");
        log.info("Compensation credit of destination wallet {} with amount {}",toWalletId,amount);
        Wallet wallet=walletRepository.findByIdWithLock(toWalletId).orElseThrow(()->new RuntimeException("Wallet not found"));
        log.info("Wallet fetched with balance {}",wallet.getBalance());
        context.put("toWalletBalanceBeforeCreditCompensation",wallet.getBalance());

        wallet.debit(amount);
        walletRepository.save(wallet);
        log.info("wallet saved with balance {}",wallet.getBalance());
        context.put("toWalletBalanceBeforeCreditCompensation",wallet.getBalance());
        log.info("Credit compensation of destination wallet step executed successfully");
        return true;
    }

    @Override
    public String getStepName() {
        return "CreditDestinationWalletStep";
    }
}
