package com.example.PaymeshLedger.saga;

import com.example.PaymeshLedger.entity.SagaInstance;

public interface ISagaOrchestrator {
    Long startSaga(SagaContext context); //Create Saga Instance and give you saga instance id

    boolean executeStep(Long sagaInstanceId, String stepName);

    boolean compensate(Long sagaInstanceId, String stepName);

    SagaInstance getSagaInstance(Long sagaInstanceId);

    void compensateSaga(Long sagaInstanceId);

    void failSaga(Long sagaInstanceId);

    void completeSaga(Long sagaInstanceId);
}
