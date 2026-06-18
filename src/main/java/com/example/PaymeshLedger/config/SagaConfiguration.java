package com.example.PaymeshLedger.config;

import com.example.PaymeshLedger.entity.SagaStep;
import com.example.PaymeshLedger.saga.ISagaStep;
import com.example.PaymeshLedger.saga.step.CreditDestinationWalletStep;
import com.example.PaymeshLedger.saga.step.DebitSourceWalletStep;
import com.example.PaymeshLedger.saga.step.SagaStepType;
import com.example.PaymeshLedger.saga.step.UpdateTransactionStatusStep;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class SagaConfiguration {

    @Bean
    public Map<String, ISagaStep> sagaStepMap(
            DebitSourceWalletStep debitSourceWalletStep,
            CreditDestinationWalletStep creditDestinationWalletStep,
            UpdateTransactionStatusStep updateTransactionStatusStep
    ){
        return Map.of(
                SagaStepType.CREDIT_DESTINATION_WALLET_STEP.toString(), creditDestinationWalletStep,
                SagaStepType.DEBIT_SOURCE_WALLET_STEP.toString(), debitSourceWalletStep,
                SagaStepType.UPDATE_TRANSACTION_STATUS_STEP.toString(), updateTransactionStatusStep
                );

    }
}
