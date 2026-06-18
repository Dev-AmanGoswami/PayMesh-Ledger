package com.example.PaymeshLedger.saga.step;

import com.example.PaymeshLedger.entity.Transaction;
import com.example.PaymeshLedger.entity.TransactionStatus;
import com.example.PaymeshLedger.repository.TransactionRepository;
import com.example.PaymeshLedger.saga.SagaContext;
import com.example.PaymeshLedger.saga.ISagaStep;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateTransactionStatusStep implements ISagaStep {
    private final TransactionRepository transactionRepository;

    private static final String TRANSACTION_ID_KEY = "transactionId";
    private static final String ORIGINAL_TRANSACTION_STATUS_KEY = "originalTransactionStatus";
    private static final String TRANSACTION_STATUS_AFTER_UPDATE_KEY = "transactionStatusAfterUpdateKey";

    @Override
    public boolean execute(SagaContext context) {
        Long transactionId = context.getLong(TRANSACTION_ID_KEY);
        log.info("Updating transaction status for transaction {}", transactionId);

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        context.put(ORIGINAL_TRANSACTION_STATUS_KEY, transaction.getStatus());

        transaction.setStatus(TransactionStatus.SUCCESS);
        transactionRepository.save(transaction);

        log.info("Transaction status updated for transaction {}", transactionId);

        context.put(TRANSACTION_STATUS_AFTER_UPDATE_KEY, transaction.getStatus());

        log.info("Update transaction status step executed successfully");

        return true;
    }

    @Override
    public boolean compensate(SagaContext context) {
        Long transactionId = context.getLong(TRANSACTION_ID_KEY);

        TransactionStatus originalTransactionStatus = TransactionStatus.valueOf(context.getString(ORIGINAL_TRANSACTION_STATUS_KEY));

        log.info("Compensating transaction status for transaction {}", transactionId);

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found."));

        transaction.setStatus(originalTransactionStatus);
        transactionRepository.save(transaction);

        log.info("Transaction status compensated for transaction {}", transactionId);

        return true;
    }

    @Override
    public String getStepName() {
        return SagaStepType.UPDATE_TRANSACTION_STATUS_STEP.toString();
    }
}
