package com.example.PaymeshLedger.saga.step;

import com.example.PaymeshLedger.entity.Wallet;
import com.example.PaymeshLedger.repository.WalletRepository;
import com.example.PaymeshLedger.saga.SagaContext;
import com.example.PaymeshLedger.saga.SagaStep;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class DebitSourceWalletStep implements SagaStep {
    private final WalletRepository walletRepository;

    private static final String STEP_NAME = "CreditDestinationWalletStep";

    private static final String FROM_WALLET_ID_KEY = "fromWalletId";
    private static final String AMOUNT_KEY = "amount";
    private static final String ORIGINAL_SOURCE_WALLET_BALANCE_KEY = "originalSourceWalletBalance";
    private static final String SOURCE_WALLET_BALANCE_AFTER_DEBIT_KEY = "fromWalletBalanceAfterDebit";
    private static final String SOURCE_WALLET_BALANCE_BEFORE_DEBIT_COMPENSATION_KEY = "fromWalletBalanceBeforeDebitCompensation";
    private static final String SOURCE_WALLET_BALANCE_AFTER_DEBIT_COMPENSATION_KEY = "fromWalletBalanceAfterDebitCompensation";

    @Override
    public boolean execute(SagaContext context) {
        Long fromWalletId = context.getLong(FROM_WALLET_ID_KEY);
        BigDecimal amount = context.getBigDecimal(AMOUNT_KEY);

        log.info("Debiting source wallet {} with amount {}", fromWalletId, amount);

        Wallet wallet = walletRepository.findByIdWithLock(fromWalletId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        log.info("Wallet fetched with balance {}", wallet.getBalance());
        context.put(ORIGINAL_SOURCE_WALLET_BALANCE_KEY, wallet.getBalance());

        wallet.debit(amount);
        walletRepository.save(wallet);

        log.info("Wallet saved with balance {}", wallet.getBalance());
        context.put(SOURCE_WALLET_BALANCE_AFTER_DEBIT_KEY, wallet.getBalance());

        log.info("Debit source wallet step executed successfully");

        return true;
    }

    @Override
    public boolean compensate(SagaContext context) {
        Long fromWalletId = context.getLong(FROM_WALLET_ID_KEY);
        BigDecimal amount = context.getBigDecimal(AMOUNT_KEY);

        log.info("Compensating source wallet {} with amount {}", fromWalletId, amount);

        Wallet wallet = walletRepository.findByIdWithLock(fromWalletId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        log.info("Wallet fetched with balance {}", wallet.getBalance());
        context.put(SOURCE_WALLET_BALANCE_BEFORE_DEBIT_COMPENSATION_KEY, wallet.getBalance());

        wallet.credit(amount);
        walletRepository.save(wallet);

        log.info("Wallet saved with balance {}", wallet.getBalance());
        context.put(SOURCE_WALLET_BALANCE_AFTER_DEBIT_COMPENSATION_KEY, wallet.getBalance());

        return true;
    }

    @Override
    public String getStepName() {
        return STEP_NAME;
    }
}
