package com.example.PaymeshLedger.controller;

import com.example.PaymeshLedger.dto.TransferRequestDto;
import com.example.PaymeshLedger.dto.TransferResponseDto;
import com.example.PaymeshLedger.entity.Transaction;
import com.example.PaymeshLedger.service.TransactionService;
import com.example.PaymeshLedger.service.TransferSagaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService transactionService;
    private final TransferSagaService transferSagaService;

    @PostMapping
    public ResponseEntity<TransferResponseDto> createTransaction(@RequestBody TransferRequestDto transferRequestDto){
        try {
            Long sagaInstanceId = transferSagaService.initiateTransfer(
                    transferRequestDto.getFromWalletId(),
                    transferRequestDto.getToWalletId(),
                    transferRequestDto.getAmount(),
                    transferRequestDto.getDescription()
            );

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(TransferResponseDto
                            .builder()
                            .sagaInstanceId(sagaInstanceId)
                            .build()
                    );
        }catch(Exception e){
            log.error("Error creating transaction", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    
}
