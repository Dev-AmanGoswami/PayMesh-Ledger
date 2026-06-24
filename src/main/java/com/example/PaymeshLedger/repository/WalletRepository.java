package com.example.PaymeshLedger.repository;

import com.example.PaymeshLedger.entity.Wallet;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {
    List<Wallet> findByUserId(Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w from Wallet w where w.userId = :userId")
    Optional<Wallet> findByUserIdWithLock(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE Wallet w SET w.balance = :balance where w.userId = :userId")
    void updateBalanceByUserId(@Param("userId") Long userId, @Param("balance") BigDecimal balance);
}
