package com.example.PaymeshLedger.repository;

import com.example.PaymeshLedger.entity.Transaction;
import com.example.PaymeshLedger.entity.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByFromWalletId(Long fromWalletId); //Debit transactions

    List<Transaction> findByToWalletId(Long toWalletId); //Credit transactions

    @Query("SELECT t from Transaction t where t.fromWalletId = :walletId OR t.toWalletId = :walletId")
    List<Transaction> findByWalletId(@Param("walletId") Long walletId);

    List<Transaction> findByStatus(TransactionStatus status);

    List<Transaction> findBySagaInstanceId(Long sagaInstanceId);
}
