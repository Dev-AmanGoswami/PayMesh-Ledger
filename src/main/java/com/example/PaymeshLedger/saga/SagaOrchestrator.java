package com.example.PaymeshLedger.saga;

import com.example.PaymeshLedger.entity.SagaInstance;

public class SagaOrchestrator implements ISagaOrchestrator{

    @Override
    public Long startSaga(SagaContext context) {
        return 0L;
    }

    @Override
    public boolean executeStep(Long sagaInstanceId, String stepName) {
        return false;
    }

    @Override
    public boolean compensate(Long sagaInstanceId, String stepName) {
        return false;
    }

    @Override
    public SagaInstance getSagaInstance(Long sagaInstanceId) {
        return null;
    }

    @Override
    public void compensateSaga(Long sagaInstanceId) {

    }

    @Override
    public void failSaga(Long sagaInstanceId) {

    }

    @Override
    public void completeSaga(Long sagaInstanceId) {

    }
}
