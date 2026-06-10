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

@Service
@RequiredArgsConstructor
public class LoanService {

    private final LoanRepository loanRepository;
    private final UserRepository userRepository;

    // VULNERABILITY (BOLA): returns every loan in the database —
    // not filtered to loans belonging to the authenticated user.
    public List<LoanResponse> listLoans() {
        return loanRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    // VULNERABILITY (BOLA): fetches by ID only — no ownership check.
    public LoanResponse getLoan(Long id) {
        return loanRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Loan not found"));
    }

    public LoanResponse applyForLoan(LoanApplicationRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        var applicant = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        var loan = Loan.builder()
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
    public LoanResponse approveLoan(Long id) {
        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Loan not found"));

        loan.setStatus(LoanStatus.APPROVED);
        return toResponse(loanRepository.save(loan));
    }

    public LoanResponse rejectLoan(Long id) {
        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Loan not found"));

        loan.setStatus(LoanStatus.REJECTED);
        return toResponse(loanRepository.save(loan));
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
