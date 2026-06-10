package com.ontestautomation.apisecurity.dto;

import com.ontestautomation.apisecurity.model.AccountType;

import java.math.BigDecimal;

public record CreateAccountRequest(AccountType type, BigDecimal initialBalance) {}
