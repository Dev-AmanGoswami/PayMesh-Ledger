package com.example.PaymeshLedger.saga;

public interface ISagaStep {
    boolean execute(SagaContext context);

    boolean compensate(SagaContext context);

    String getStepName();
}
