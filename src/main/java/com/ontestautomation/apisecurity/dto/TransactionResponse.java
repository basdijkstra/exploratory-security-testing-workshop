package com.ontestautomation.apisecurity.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse(
        String id,
        String fromAccountId,
        String toAccountId,
        BigDecimal amount,
        String description,
        LocalDateTime createdAt
) {}
