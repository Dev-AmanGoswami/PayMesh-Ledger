package com.example.PaymeshLedger.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "wallet")
public class Wallet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "balance", nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;

    public boolean hasSufficientBalance(BigDecimal amount){
        return balance.compareTo(amount) >= 0;
    }

    public void debit(BigDecimal amount){
        if(!hasSufficientBalance(amount)){
            throw new IllegalArgumentException("Insufficient Balance");
        }
        balance = balance.subtract(amount);
    }

    public void credit(BigDecimal amount){
        balance = balance.add(amount);
    }
}