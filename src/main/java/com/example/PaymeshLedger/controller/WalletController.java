package com.example.PaymeshLedger.controller;

import com.example.PaymeshLedger.dto.CreateWalletRequest;
import com.example.PaymeshLedger.dto.CreditWalletRequest;
import com.example.PaymeshLedger.dto.DebitWalletRequest;
import com.example.PaymeshLedger.entity.Wallet;
import com.example.PaymeshLedger.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Slf4j
@RestController
@RequestMapping("/wallets")
@RequiredArgsConstructor
public class WalletController {
    private final WalletService walletService;

    @PostMapping
    public ResponseEntity<Wallet> createWallet(@RequestBody CreateWalletRequest request){
        try{
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(walletService.createWallet(request.getUserId()));
        }catch(Exception e){
            log.error("Error creating wallet", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Wallet> getWalletById(@PathVariable Long id){
        return ResponseEntity.ok(walletService.getWalletById(id));
    }

    @GetMapping("/{id}/balance")
    public ResponseEntity<BigDecimal> getWalletBalance(@PathVariable Long id){
        BigDecimal balance = walletService.getWalletBalance(id);
        return ResponseEntity.ok(balance);
    }

    @PostMapping("/{userId}/debit")
    public ResponseEntity<Wallet> debitWallet(@PathVariable Long userId, @RequestBody DebitWalletRequest request){
        walletService.debit(userId, request.getAmount());
        Wallet wallet = walletService.getWalletByUserId(userId);
        return ResponseEntity.ok(wallet);
    }

    @PostMapping("/{userId}/credit")
    public ResponseEntity<Wallet> createWallet(@PathVariable Long userId, @RequestBody CreditWalletRequest request){
        walletService.credit(userId, request.getAmount());
        Wallet wallet = walletService.getWalletByUserId(userId);
        return ResponseEntity.ok(wallet);
    }
}
