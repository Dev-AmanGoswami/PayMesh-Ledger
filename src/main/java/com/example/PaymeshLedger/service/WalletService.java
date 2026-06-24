package com.example.PaymeshLedger.service;

import com.example.PaymeshLedger.entity.Wallet;
import com.example.PaymeshLedger.repository.WalletRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletService {
    private final WalletRepository walletRepository;

    public Wallet createWallet(Long userId){
        log.info("Creating wallet for user {}", userId);

        Wallet wallet = Wallet.builder()
                .userId(userId)
                .isActive(true)
                .balance(BigDecimal.ZERO)
                .build();

        wallet = walletRepository.save(wallet);
        log.info("Wallet created with id {}", wallet.getId());
        return wallet;
    }

    public Wallet getWalletById(Long id){
        return walletRepository.findById(id).orElseThrow(() -> new RuntimeException("Wallet not found"));
    }

    public Wallet getWalletByUserId(Long userId){
        return walletRepository.findByUserId(userId).getFirst();
    }

    @Transactional
    public void debit(Long userId, BigDecimal amount){
        log.info("Debiting {} from user {}", amount, userId);
        Wallet wallet = getWalletByUserId(userId);
        walletRepository.updateBalanceByUserId(userId, wallet.getBalance().subtract(amount));

        log.info("Debit successful from user {}", userId);
    }

    @Transactional
    public void credit(Long userId, BigDecimal amount){
        log.info("Crediting {} to user {}", amount, userId);
        Wallet wallet = getWalletByUserId(userId);
        walletRepository.updateBalanceByUserId(userId, wallet.getBalance().add(amount));

        log.info("Credit successful to user {}", userId);
    }

    public BigDecimal getWalletBalance(Long walletId){
        log.info("Getting balance for wallet {}", walletId);
        Wallet wallet = getWalletById(walletId);
        BigDecimal balance = wallet.getBalance();
        log.info("Balance for wallet {} is {}", walletId, balance);

        return balance;
    }
}
