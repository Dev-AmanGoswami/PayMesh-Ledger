package com.example.PaymeshLedger.service;

import com.example.PaymeshLedger.entity.Transaction;
import com.example.PaymeshLedger.saga.SagaContext;
import com.example.PaymeshLedger.saga.SagaOrchestrator;
import com.example.PaymeshLedger.saga.step.SagaStepFactory;
import com.example.PaymeshLedger.saga.step.SagaStepType;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransferSagaService {
    private final TransactionService transactionService;
    private final SagaOrchestrator sagaOrchestrator;

    @Transactional
    public Long initiateTransfer(
        Long fromWalletId,
        Long toWalletId,
        BigDecimal amount,
        String description
    ){
        log.info("Initiating transfer from wallet {} to wallet {} with amount {} and description {}", fromWalletId, toWalletId, amount, description);
        Transaction transaction = transactionService.createTransaction(fromWalletId, toWalletId, amount, description);
        SagaContext sagaContext = SagaContext.builder()
                .data(Map.ofEntries(
                    Map.entry("transactionId", transaction.getId()),
                    Map.entry("fromWalletId", fromWalletId),
                    Map.entry("toWalletId", toWalletId),
                    Map.entry("amount", amount),
                    Map.entry("description", description)
                ))
                .build();

        Long sagaInstanceId = sagaOrchestrator.startSaga(sagaContext);
        log.info("Saga instance created with id {}", sagaInstanceId);

        transactionService.updateTransactionWithSagaInstanceId(transaction.getId(), sagaInstanceId);
        executeTransferSaga(sagaInstanceId);
        return sagaInstanceId;
    }

    public void executeTransferSaga(Long sagaInstanceId){
        log.info("Executing transfer saga with id {}", sagaInstanceId);
        try{
            for(SagaStepType step: SagaStepFactory.TransferMoneySagaSteps){
                boolean success = sagaOrchestrator.executeStep(sagaInstanceId, step.toString());
                if(!success){
                    log.error("Failed to execute step {}", step.toString());
                    sagaOrchestrator.failSaga(sagaInstanceId);
                    return;
                }
            }
            sagaOrchestrator.compensateSaga(sagaInstanceId);
            log.info("Transfer saga completed with id {}", sagaInstanceId);
        }catch(Exception e){
            log.error("Failed to execute transfer saga with id {}", sagaInstanceId, e);
            sagaOrchestrator.failSaga(sagaInstanceId);
        }
    }
}
