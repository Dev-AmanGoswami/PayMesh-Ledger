package com.example.PaymeshLedger.saga.step;

import com.example.PaymeshLedger.saga.ISagaStep;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class SagaStepFactory {
    private final Map<String, ISagaStep> sagaStepMap;

    public static final List<SagaStepType> TransferMoneySagaSteps = List.of(
        SagaStepType.DEBIT_SOURCE_WALLET_STEP,
        SagaStepType.CREDIT_DESTINATION_WALLET_STEP,
        SagaStepType.UPDATE_TRANSACTION_STATUS_STEP
    );


    public ISagaStep getSagaStep(String stepName){
       return sagaStepMap.get(stepName);
    }
}
