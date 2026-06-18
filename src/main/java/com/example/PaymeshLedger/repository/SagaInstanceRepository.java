package com.example.PaymeshLedger.repository;

import com.example.PaymeshLedger.entity.SagaInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SagaInstanceRepository extends JpaRepository<SagaInstance, Long> {

}