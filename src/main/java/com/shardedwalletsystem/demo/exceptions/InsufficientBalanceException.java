package com.shardedwalletsystem.demo.exceptions;

public class InsufficientBalanceException extends WalletException {
    public InsufficientBalanceException(String message) {
        super(message);
    }
}
