package com.ontestautomation.apisecurity.dto;

import com.ontestautomation.apisecurity.model.LoanStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record LoanResponse(
        String id,
        String applicantUsername,
        BigDecimal amount,
        LoanStatus status,
        LocalDateTime requestedAt
) {}
