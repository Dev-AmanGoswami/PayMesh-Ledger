package com.example.PaymeshLedger.repository;

import com.example.PaymeshLedger.entity.SagaStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SagaStepRepository extends JpaRepository<SagaStep, Long> {
    List<SagaStep> findBySagaInstanceId(Long sagaInstanceId);

    @Query("SELECT s FROM SagaStep s where s.sagaInstanceId = :sagaInstanceId AND s.status = 'COMPLETED'")
    List<SagaStep> findCompletedStepsBySagaInstanceId(@Param("sagaInstanceId") Long sagaInstanceId);

    @Query("SELECT s FROM SagaStep s where s.sagaInstanceId = :sagaInstanceId AND s.status IN ('COMPLETED', 'COMPENSATED')")
    List<SagaStep> findCompletedOrCompensatedStepsBySagaInstanceId(@Param("sagaInstanceId") Long sagaInstanceId);
}