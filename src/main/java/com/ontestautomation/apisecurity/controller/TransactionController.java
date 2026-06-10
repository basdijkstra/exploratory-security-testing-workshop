package com.ontestautomation.apisecurity.controller;

import com.ontestautomation.apisecurity.dto.TransactionReport;
import com.ontestautomation.apisecurity.dto.TransactionResponse;
import com.ontestautomation.apisecurity.dto.TransferRequest;
import com.ontestautomation.apisecurity.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<TransactionResponse> getTransaction(@PathVariable Long id) {
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
    public ResponseEntity<List<TransactionReport>> report() {
        return ResponseEntity.ok(transactionService.generateReport());
    }
}
