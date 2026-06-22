package com.example.PaymeshLedger.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "saga_instance")
public class SagaInstance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SagaStatus status = SagaStatus.STARTED;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "context", columnDefinition = "json")
    private String context;

    @Column(name = "current_step")
    private String currentStep;

    public void markAsStarted(){
        this.status = SagaStatus.STARTED;
    }

    public void markAsPending(){
        this.status = SagaStatus.PENDING;
    }

    public void markAsRunning(){
        this.status = SagaStatus.RUNNING;
    }

    public void markAsCompleted(){
        this.status = SagaStatus.COMPLETED;
    }

    public void markAsFailed(){
        this.status = SagaStatus.FAILED;
    }

    public void markAsCompensating(){
        this.status = SagaStatus.COMPENSATING;
    }

    public void markAsCompensated(){
        this.status = SagaStatus.COMPENSATED;
    }
}

