package com.ontestautomation.apisecurity.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse(
        Long id,
        Long fromAccountId,
        Long toAccountId,
        BigDecimal amount,
        String description,
        LocalDateTime createdAt
) {}
