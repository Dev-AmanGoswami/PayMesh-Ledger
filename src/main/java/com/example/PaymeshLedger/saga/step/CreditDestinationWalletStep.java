package com.example.PaymeshLedger.saga.step;

import com.example.PaymeshLedger.entity.Wallet;
import com.example.PaymeshLedger.repository.WalletRepository;
import com.example.PaymeshLedger.saga.SagaContext;
import com.example.PaymeshLedger.saga.ISagaStep;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreditDestinationWalletStep implements ISagaStep {
    private final WalletRepository walletRepository;

    private static final String TO_WALLET_ID_KEY = "toWalletId";
    private static final String AMOUNT_KEY = "amount";
    private static final String ORIGINAL_TO_WALLET_BALANCE_KEY = "originalToWalletBalance";
    private static final String TO_WALLET_BALANCE_AFTER_CREDIT_KEY = "toWalletBalanceAfterCredit";
    private static final String TO_WALLET_BALANCE_AFTER_CREDIT_COMPENSATION_KEY = "toWalletBalanceAfterCreditCompensation";

    @Override
    @Transactional
    public boolean execute(SagaContext context){
        //Step 1 Get the destination wallet id from the context
        Long toWalletId = context.getLong(TO_WALLET_ID_KEY);

        BigDecimal amount = context.getBigDecimal(AMOUNT_KEY);

        log.info("Crediting destination wallet {} with amount {}", toWalletId, amount);

        //Step 2 Fetch the destination wallet from the database with a lock
        Wallet wallet = walletRepository.findByUserIdWithLock(toWalletId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        log.info("Wallet fetched with balance {}", wallet.getBalance());
        context.put(ORIGINAL_TO_WALLET_BALANCE_KEY, wallet.getBalance());

        //Step 3 Credit the destination wallet
        walletRepository.updateBalanceByUserId(toWalletId, wallet.getBalance().add(amount));

        log.info("Wallet saved with balance {}", wallet.getBalance());
        context.put(TO_WALLET_BALANCE_AFTER_CREDIT_KEY, wallet.getBalance());

        log.info("Credit destination wallet step executed successfully");
        return true;
    }

    @Override
    @Transactional
    public boolean compensate(SagaContext context){
        //Step 1 Get the destination wallet id from the context
        Long toWalletId = context.getLong(TO_WALLET_ID_KEY);

        BigDecimal amount = context.getBigDecimal(AMOUNT_KEY);

        log.info("Compensation credit of destination wallet {} with amount {}", toWalletId, amount);

        //Step 2 Fetch the destination wallet from the database with a lock
        Wallet wallet = walletRepository.findByUserIdWithLock(toWalletId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        log.info("Wallet fetched with balance {}", wallet.getBalance());

        //Step 3 Debit the destination wallet
        walletRepository.updateBalanceByUserId(toWalletId, wallet.getBalance().subtract(amount));

        log.info("Wallet saved with balance {}", wallet.getBalance());
        context.put(TO_WALLET_BALANCE_AFTER_CREDIT_COMPENSATION_KEY, wallet.getBalance());

        log.info("Credit compensation destination wallet step executed successfully");
        return true;
    }

    @Override
    public String getStepName(){
        return SagaStepType.CREDIT_DESTINATION_WALLET_STEP.toString();
    }
}
