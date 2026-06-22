package com.example.PaymeshLedger.service;

import com.example.PaymeshLedger.entity.SagaStatus;
import com.example.PaymeshLedger.entity.Transaction;
import com.example.PaymeshLedger.entity.TransactionStatus;
import com.example.PaymeshLedger.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository transactionRepository;

    @Transactional
    public Transaction createTransaction(Long fromWalletId, Long toWalletId, BigDecimal amount, String description){
        log.info("Creating transaction from wallet {} to wallet {} with amount {} and description {}", fromWalletId, toWalletId, amount, description);
        return transactionRepository.save(
            Transaction.builder()
                    .fromWalletId(fromWalletId)
                    .toWalletId(toWalletId)
                    .amount(amount)
                    .description(description)
                    .build()
        );
    }

    public Transaction getTransactionById(Long id){
        return transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found with id " + id));
    }

    public List<Transaction> getTransactionByWalletId(Long walletId){
        return transactionRepository.findByWalletId(walletId);
    }

    public List<Transaction> getTransactionByFromWalletId(Long fromWalletId){
        return transactionRepository.findByFromWalletId(fromWalletId);
    }

    public List<Transaction> getTransactionByToWalletId(Long toWalletId){
        return transactionRepository.findByToWalletId(toWalletId);
    }

    public List<Transaction> getTransactionBySagaInstanceId(Long sagaInstanceId){
        return transactionRepository.findBySagaInstanceId(sagaInstanceId);
    }

    public List<Transaction> getTransactionByStatus(TransactionStatus status){
        return transactionRepository.findByStatus(status);
    }
}
