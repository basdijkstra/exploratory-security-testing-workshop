package com.ontestautomation.apisecurity.service;

import com.ontestautomation.apisecurity.dto.AccountResponse;
import com.ontestautomation.apisecurity.dto.CreateAccountRequest;
import com.ontestautomation.apisecurity.model.Account;
import com.ontestautomation.apisecurity.model.Role;
import com.ontestautomation.apisecurity.model.User;
import com.ontestautomation.apisecurity.repository.AccountRepository;
import com.ontestautomation.apisecurity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    // VULNERABILITY (BOLA): fetches by ID only — no check that the
    // account belongs to the authenticated user.
    public AccountResponse getAccount(String id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));
        if (isCustomer() && account.getOwner().getRole() == Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found");
        }
        return toResponse(account);
    }

    public AccountResponse createAccount(CreateAccountRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        var owner = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        var account = Account.builder()
                .id(generateId())
                .accountNumber(generateAccountNumber())
                .type(request.type())
                .balance(java.math.BigDecimal.ZERO)
                .owner(owner)
                .build();

        return toResponse(accountRepository.save(account));
    }

    // VULNERABILITY (BOLA): deletes by ID only — no ownership check.
    public void deleteAccount(String id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));
        if (isCustomer() && account.getOwner().getRole() == Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found");
        }
        accountRepository.deleteById(id);
    }

    public AccountResponse toResponse(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getAccountNumber(),
                account.getType(),
                account.getBalance(),
                account.getOwner().getUsername()
        );
    }

    private boolean isCustomer() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User requester = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        return requester.getRole() == Role.CUSTOMER;
    }

    private String generateId() {
        int n = 100000 + ThreadLocalRandom.current().nextInt(900000);
        return "account-" + n;
    }

    private String generateAccountNumber() {
        long count = accountRepository.count() + 1;
        return String.format("NL10BANK%010d", count);
    }
}
