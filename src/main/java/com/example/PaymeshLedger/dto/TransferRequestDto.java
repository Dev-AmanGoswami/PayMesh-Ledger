package com.example.PaymeshLedger.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransferRequestDto {
    private Long fromWalletId; //fromUserId
    private Long toWalletId; //toUserId
    private BigDecimal amount;
    private String description;
}
