package com.shardedwalletsystem.demo.repository;

import com.shardedwalletsystem.demo.model.Transaction;
import com.shardedwalletsystem.demo.model.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByFromWalletId(Long fromWalletId); // all the debit transactions
    List<Transaction> findByToWalletId(Long toWalletId); // all the credit transactions
    @Query("SELECT t FROM Transaction t WHERE t.fromWalletId=:walletId OR t.toWalletId=:walletId")
    List<Transaction> findByWalletId(@Param("walletId") Long walletId);
    List<Transaction> findByStatus(TransactionStatus status);
    List<Transaction> findBySagaInstanceId(Long sagaInstanceId);
}
