package com.ontestautomation.apisecurity.dto;

import java.math.BigDecimal;

public record TransferRequest(
        String fromAccountId,
        String toAccountId,
        BigDecimal amount,
        String description
) {}
