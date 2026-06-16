package com.example.PaymeshLedger.saga.step;

import com.example.PaymeshLedger.repository.TransactionRepository;
import com.example.PaymeshLedger.saga.SagaContext;
import com.example.PaymeshLedger.saga.SagaStep;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UpdateTransactionStatusStep implements SagaStep {
    private final TransactionRepository transactionRepository;

    private static final String TRANSACTION_ID_KEY = "transactionId";

    @Override
    public boolean execute(SagaContext context) {
        Long transactionId = context.getLong(TRANSACTION_ID_KEY);

        return true;
    }

    @Override
    public boolean compensate(SagaContext context) {
        return true;
    }

    @Override
    public String getStepName() {
        return "";
    }
}
