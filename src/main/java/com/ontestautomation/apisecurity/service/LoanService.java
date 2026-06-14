package com.ontestautomation.apisecurity.service;

import com.ontestautomation.apisecurity.dto.LoanApplicationRequest;
import com.ontestautomation.apisecurity.dto.LoanResponse;
import com.ontestautomation.apisecurity.model.Loan;
import com.ontestautomation.apisecurity.model.LoanStatus;
import com.ontestautomation.apisecurity.repository.LoanRepository;
import com.ontestautomation.apisecurity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class LoanService {

    private final LoanRepository loanRepository;
    private final UserRepository userRepository;

    public List<LoanResponse> listLoans() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        var applicant = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        return loanRepository.findByApplicant(applicant).stream()
                .map(this::toResponse)
                .toList();
    }

    // VULNERABILITY (BOLA): fetches by ID only — no ownership check.
    public LoanResponse getLoan(String id) {
        return loanRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Loan not found"));
    }

    public LoanResponse applyForLoan(LoanApplicationRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        var applicant = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        var loan = Loan.builder()
                .id(generateId())
                .applicant(applicant)
                .amount(request.amount())
                .status(LoanStatus.PENDING)
                .requestedAt(LocalDateTime.now())
                .build();

        return toResponse(loanRepository.save(loan));
    }

    // VULNERABILITY (Sensitive Business Flow): the role check is performed
    // in the controller, but there is no check that the approver is not the
    // same person as the applicant. A user who forges an admin token (via the
    // weak JWT secret) can approve their own loan.
    public LoanResponse approveLoan(String id) {
        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Loan not found"));
        if (loan.getStatus() != LoanStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only pending loans can be approved");
        }
        loan.setStatus(LoanStatus.APPROVED);
        return toResponse(loanRepository.save(loan));
    }

    public LoanResponse rejectLoan(String id) {
        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Loan not found"));
        if (loan.getStatus() != LoanStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only pending loans can be rejected");
        }
        loan.setStatus(LoanStatus.REJECTED);
        return toResponse(loanRepository.save(loan));
    }

    private String generateId() {
        int n = 100000 + ThreadLocalRandom.current().nextInt(900000);
        return "loan-" + n;
    }

    private LoanResponse toResponse(Loan loan) {
        return new LoanResponse(
                loan.getId(),
                loan.getApplicant().getUsername(),
                loan.getAmount(),
                loan.getStatus(),
                loan.getRequestedAt()
        );
    }
}
