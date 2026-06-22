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

    public List<Wallet> getWalletByUserId(Long userId){
        return walletRepository.findByUserId(userId);
    }

    @Transactional
    public void debit(Long walletId, BigDecimal amount){
        log.info("Debiting {} from wallet {}", amount, walletId);
        Wallet wallet = getWalletById(walletId);
        wallet.debit(amount);

        walletRepository.save(wallet);
        log.info("Debit successful from wallet {}", walletId);
    }

    @Transactional
    public void credit(Long walletId, BigDecimal amount){
        log.info("Crediting {} to wallet {}", amount, walletId);
        Wallet wallet = getWalletById(walletId);
        wallet.credit(amount);

        walletRepository.save(wallet);
        log.info("Credit successful to wallet {}", walletId);
    }

    public BigDecimal getWalletBalance(Long walletId){
        log.info("Getting balance for wallet {}", walletId);
        Wallet wallet = getWalletById(walletId);
        BigDecimal balance = wallet.getBalance();
        log.info("Balance for wallet {} is {}", walletId, balance);

        return balance;
    }
}
