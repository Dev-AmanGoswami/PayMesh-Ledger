package com.example.PaymeshLedger.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "step")
public class SagaStep {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "saga_instance_id", nullable = false)
    private Long sagaInstanceId;

    @Column(name = "step_name", nullable = false)
    private String stepName;

    @Column(name = "status", nullable = false)
    private StepStatus status;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "step_data", columnDefinition = "json")
    private String stepData;

    public void markAsPending(){
        this.status = StepStatus.PENDING;
    }

    public void markAsRunning(){
        this.status = StepStatus.RUNNING;
    }

    public void markAsCompleted(){
        this.status = StepStatus.COMPLETED;
    }

    public void markAsFailed(){
        this.status = StepStatus.FAILED;
    }

    public void markAsCompensating(){
        this.status = StepStatus.COMPENSATING;
    }

    public void markAsCompensated(){
        this.status = StepStatus.COMPENSATED;
    }

    public void markAsSkipped(){
        this.status = StepStatus.SKIPPED;
    }
}
