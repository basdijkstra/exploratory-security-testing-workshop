package com.ontestautomation.apisecurity.controller;

import com.ontestautomation.apisecurity.dto.TransactionResponse;
import com.ontestautomation.apisecurity.dto.TransferRequest;
import com.ontestautomation.apisecurity.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping
    public ResponseEntity<List<TransactionResponse>> listTransactions(
            @RequestParam(required = false) String search) {
        if (search != null) {
            return ResponseEntity.ok(transactionService.searchTransactions(search));
        }
        return ResponseEntity.ok(transactionService.listTransactions());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> getTransaction(@PathVariable String id) {
        return ResponseEntity.ok(transactionService.getTransaction(id));
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransactionResponse> transfer(@RequestBody TransferRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionService.transfer(request));
    }

    @PostMapping("/bulk-transfer")
    public ResponseEntity<List<TransactionResponse>> bulkTransfer(
            @RequestBody List<TransferRequest> requests) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionService.bulkTransfer(requests));
    }

    @GetMapping("/report")
    public ResponseEntity<?> report(Authentication auth) {
        if (!auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Admin access required"));
        }
        return ResponseEntity.ok(transactionService.generateReport());
    }
}
