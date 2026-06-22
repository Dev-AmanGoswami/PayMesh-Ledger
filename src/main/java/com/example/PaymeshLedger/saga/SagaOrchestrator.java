package com.example.PaymeshLedger.saga;

import com.example.PaymeshLedger.entity.SagaInstance;
import com.example.PaymeshLedger.entity.SagaStep;
import com.example.PaymeshLedger.entity.StepStatus;
import com.example.PaymeshLedger.repository.SagaInstanceRepository;
import com.example.PaymeshLedger.repository.SagaStepRepository;
import com.example.PaymeshLedger.saga.step.SagaStepFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SagaOrchestrator implements ISagaOrchestrator{
    private final ObjectMapper objectMapper;
    private final SagaInstanceRepository sagaInstanceRepository;
    private final SagaStepFactory sagaStepFactory;
    private final SagaStepRepository sagaStepRepository;

    @Override
    public Long startSaga(SagaContext context) {
        try{
            String contextJson = objectMapper.writeValueAsString(context);
            SagaInstance sagaInstance = SagaInstance.builder()
                    .context(contextJson)
                    .build();
            sagaInstance.markAsStarted();
            sagaInstance = sagaInstanceRepository.save(sagaInstance);

            log.info("Started saga with id {}", sagaInstance.getId());
            return sagaInstance.getId();
        }catch(Exception e){
            log.error("Error starting saga: {}", e.getMessage());
            throw new RuntimeException("Error starting saga", e);
        }
    }

    @Override
    @Transactional
    public boolean executeStep(Long sagaInstanceId, String stepName) {
        SagaInstance sagaInstance = sagaInstanceRepository.findById(sagaInstanceId)
                .orElseThrow(() -> new RuntimeException("Saga instance not found"));

        ISagaStep sagaStep = sagaStepFactory.getSagaStep(stepName);
        if(sagaStep == null){
            log.error("Saga step not found for step name {}", stepName);
            throw new RuntimeException("Saga step not found");
        }

        SagaStep sagaStepDB = sagaStepRepository.findBySagaInstanceIdAndStepNameAndStatus(sagaInstanceId, stepName, StepStatus.PENDING)
                .orElse(sagaStepRepository.save(SagaStep.builder().sagaInstanceId(sagaInstanceId).stepName(stepName).status(StepStatus.PENDING).build()));

        try{
            SagaContext sagaContext = objectMapper.readValue(sagaInstance.getContext(), SagaContext.class);
            sagaStepDB.markAsRunning();
            sagaStepRepository.save(sagaStepDB);

            boolean success = sagaStep.execute(sagaContext);
            if(success) {
                sagaStepDB.markAsCompleted();
                sagaStepRepository.save(sagaStepDB);

                sagaInstance.setCurrentStep(stepName);
                sagaInstance.markAsRunning();
                sagaInstanceRepository.save(sagaInstance);

                log.info("Step {} executed successfully", stepName);

                return true;
            }else{
                sagaStepDB.markAsFailed();
                sagaStepRepository.save(sagaStepDB);
                log.error("Step {} failed", stepName);

                return false;
            }
        }catch(Exception e){
            sagaStepDB.markAsFailed();
            sagaStepRepository.save(sagaStepDB);

            log.error("Failed to execute saga step {}", stepName, e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean compensateStep(Long sagaInstanceId, String stepName) {
        SagaInstance sagaInstance = sagaInstanceRepository.findById(sagaInstanceId)
                .orElseThrow(() -> new RuntimeException("Saga instance not found"));

        ISagaStep sagaStep = sagaStepFactory.getSagaStep(stepName);
        if(sagaStep == null){
            log.error("Saga step not found for step name {}", stepName);
            throw new RuntimeException("Saga step not found");
        }

        SagaStep sagaStepDB = sagaStepRepository.findBySagaInstanceIdAndStepNameAndStatus(sagaInstanceId, stepName, StepStatus.COMPENSATED)
                .orElse(null);

        if(sagaStepDB == null){
            log.warn("Step {} not found int the db for saga instance {}, so it is already compensated or not executed", stepName, sagaInstanceId);
            return true;
        }

        try{
            SagaContext sagaContext = objectMapper.readValue(sagaInstance.getContext(), SagaContext.class);
            sagaStepDB.markAsCompensating();
            sagaStepRepository.save(sagaStepDB);

            boolean success = sagaStep.compensate(sagaContext);
            if(success){
                sagaStepDB.markAsCompensated();
                sagaStepRepository.save(sagaStepDB);

                log.info("Step {} compensated successfully", stepName);
                return true;
            }else{
                sagaStepDB.markAsFailed();
                sagaStepRepository.save(sagaStepDB);
                log.error("Failed to execute step {}", stepName);

                return false;
            }
        }catch(Exception e){
            sagaStepDB.markAsFailed();
            sagaStepRepository.save(sagaStepDB);

            log.error("Failed to execute step {}", stepName);
            return false;
        }
    }

    @Override
    public SagaInstance getSagaInstance(Long sagaInstanceId) {
        return sagaInstanceRepository.findById(sagaInstanceId)
                .orElseThrow(() -> new RuntimeException("Saga instance not found with id: " + sagaInstanceId));
    }

    @Override
    @Transactional
    public void compensateSaga(Long sagaInstanceId) {
        SagaInstance sagaInstance = sagaInstanceRepository.findById(sagaInstanceId).orElseThrow(() -> new RuntimeException("Saga instance not found with id: " + sagaInstanceId));
        sagaInstance.markAsCompensating();

        List<SagaStep> completedSteps = sagaStepRepository.findCompletedStepsBySagaInstanceId(sagaInstanceId);

        boolean allStepsCompensated = true;
        for(SagaStep sagaStep: completedSteps){
            boolean compensated = this.compensateStep(sagaInstanceId, sagaStep.getStepName());
            if(!compensated){
                allStepsCompensated = false;
                break;
            }
        }

        if(allStepsCompensated){
            sagaInstance.markAsCompensated();
            sagaInstanceRepository.save(sagaInstance);
            log.info("Saga {} compensated successfully", sagaInstanceId);
        }else{
            log.error("Saga {} compensation failed", sagaInstanceId);
        }
    }

    @Override
    @Transactional
    public void failSaga(Long sagaInstanceId) {
        SagaInstance sagaInstance = sagaInstanceRepository.findById(sagaInstanceId).orElseThrow(() -> new RuntimeException("Saga instance not found with id: " + sagaInstanceId));
        sagaInstance.markAsFailed();
        sagaInstanceRepository.save(sagaInstance);
    }

    @Override
    @Transactional
    public void completeSaga(Long sagaInstanceId) {
        SagaInstance sagaInstance = sagaInstanceRepository.findById(sagaInstanceId).orElseThrow(() -> new RuntimeException("Saga instance not found with id: " + sagaInstanceId));
        sagaInstance.markAsCompleted();
        sagaInstanceRepository.save(sagaInstance);
    }
}
