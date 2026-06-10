package com.ontestautomation.apisecurity.dto;

import com.ontestautomation.apisecurity.model.AccountType;

import java.math.BigDecimal;

public record AccountResponse(
        Long id,
        String accountNumber,
        AccountType type,
        BigDecimal balance,
        String ownerUsername
) {}
