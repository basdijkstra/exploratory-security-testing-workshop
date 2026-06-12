package com.ontestautomation.apisecurity.controller;

import com.ontestautomation.apisecurity.dto.LoanApplicationRequest;
import com.ontestautomation.apisecurity.dto.LoanResponse;
import com.ontestautomation.apisecurity.service.LoanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;

    @GetMapping
    public ResponseEntity<List<LoanResponse>> listLoans() {
        return ResponseEntity.ok(loanService.listLoans());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LoanResponse> getLoan(@PathVariable Long id) {
        return ResponseEntity.ok(loanService.getLoan(id));
    }

    @PostMapping("/apply")
    public ResponseEntity<LoanResponse> applyForLoan(@RequestBody LoanApplicationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(loanService.applyForLoan(request));
    }

    // The role check here is correct — only ROLE_ADMIN may approve.
    // VULNERABILITY (Sensitive Business Flow): there is no check that the
    // approver is a different person from the applicant. A user who obtains
    // an admin-role token (by exploiting the weak JWT secret) can approve
    // their own pending loan.
    @PostMapping("/{id}/approve")
    public ResponseEntity<?> approveLoan(@PathVariable Long id, Authentication auth) {
        if (!auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Admin access required"));
        }
        return ResponseEntity.ok(loanService.approveLoan(id));
    }

    // VULNERABILITY (BFLA): any authenticated user can reject any loan —
    // the admin check present on approveLoan() is missing here.
    @PostMapping("/{id}/reject")
    public ResponseEntity<?> rejectLoan(@PathVariable Long id) {
        return ResponseEntity.ok(loanService.rejectLoan(id));
    }
}
