package com.example.PaymeshLedger.saga.step;

import com.example.PaymeshLedger.saga.ISagaStep;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class SagaStepFactory {
    private final Map<String, ISagaStep> sagaStepMap;

    public ISagaStep getSagaStep(String stepName){
       return sagaStepMap.get(stepName);
    }
}
