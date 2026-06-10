package com.ontestautomation.apisecurity.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionReport(
        Long id,
        String fromAccountNumber,
        String fromOwner,
        String toAccountNumber,
        String toOwner,
        BigDecimal amount,
        String description,
        LocalDateTime createdAt
) {}
