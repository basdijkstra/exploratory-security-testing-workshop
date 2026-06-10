package com.ontestautomation.apisecurity.dto;

import java.math.BigDecimal;

public record TransferRequest(
        Long fromAccountId,
        Long toAccountId,
        BigDecimal amount,
        String description
) {}
