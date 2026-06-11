package com.ontestautomation.apisecurity.service;

import com.ontestautomation.apisecurity.dto.TransactionReport;
import com.ontestautomation.apisecurity.dto.TransactionResponse;
import com.ontestautomation.apisecurity.dto.TransferRequest;
import com.ontestautomation.apisecurity.model.Account;
import com.ontestautomation.apisecurity.model.Transaction;
import com.ontestautomation.apisecurity.model.User;
import com.ontestautomation.apisecurity.repository.AccountRepository;
import com.ontestautomation.apisecurity.repository.TransactionRepository;
import com.ontestautomation.apisecurity.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public List<TransactionResponse> listTransactions() {
        return transactionRepository.findByUser(currentUser()).stream()
                .map(this::toResponse)
                .toList();
    }

    // VULNERABILITY (SQL Injection): the search keyword is concatenated
    // directly into the native SQL query without sanitisation.
    // The account ID filter is built safely, but keyword is not parameterised,
    // so an attacker can break out of the LIKE clause and read all transactions.
    // Payload example: ' OR 1=1--
    @SuppressWarnings("unchecked")
    public List<TransactionResponse> searchTransactions(String keyword) {
        List<String> accountIds = accountRepository.findByOwner(currentUser()).stream()
                .map(Account::getId)
                .toList();

        if (accountIds.isEmpty()) {
            return List.of();
        }

        String ids = accountIds.stream().map(id -> "'" + id + "'").collect(Collectors.joining(","));
        String sql = "SELECT * FROM transactions " +
                     "WHERE (from_account_id IN (" + ids + ") OR to_account_id IN (" + ids + ")) " +
                     "AND description LIKE '%" + keyword + "%'";

        List<Transaction> results = entityManager.createNativeQuery(sql, Transaction.class).getResultList();
        return results.stream().map(this::toResponse).toList();
    }

    // VULNERABILITY (BOLA): fetches by ID only — no ownership check.
    public TransactionResponse getTransaction(Long id) {
        return transactionRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction not found"));
    }

    // VULNERABILITY (Sensitive Business Flow):
    //   - No maximum transfer amount enforced.
    //   - No daily limit check.
    //   - No ownership check on fromAccount — any authenticated user can
    //     drain any account by its ID.
    //   - Balance is allowed to go negative (no overdraft protection).
    @Transactional
    public TransactionResponse transfer(TransferRequest request) {
        Account from = accountRepository.findById(request.fromAccountId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Source account not found"));
        Account to = accountRepository.findById(request.toAccountId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Destination account not found"));

        from.setBalance(from.getBalance().subtract(request.amount()));
        to.setBalance(to.getBalance().add(request.amount()));
        accountRepository.save(from);
        accountRepository.save(to);

        Transaction tx = Transaction.builder()
                .fromAccount(from)
                .toAccount(to)
                .amount(request.amount())
                .description(request.description() != null ? request.description() : "Transfer")
                .createdAt(LocalDateTime.now())
                .build();

        return toResponse(transactionRepository.save(tx));
    }

    // VULNERABILITY (Unrestricted Resource Consumption + Sensitive Business Flow):
    //   - No limit on the number of transfers in a single request.
    //   - Each transfer is subject to the same missing controls as transfer().
    @Transactional
    public List<TransactionResponse> bulkTransfer(List<TransferRequest> requests) {
        return requests.stream().map(this::transfer).toList();
    }

    // VULNERABILITY (Unrestricted Resource Consumption):
    //   - No pagination — returns the full denormalised transaction history.
    //   - No rate limiting — can be called repeatedly to degrade the service.
    public List<TransactionReport> generateReport() {
        return transactionRepository.findAll().stream()
                .map(this::toReport)
                .toList();
    }

    private User currentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }

    private TransactionResponse toResponse(Transaction tx) {
        return new TransactionResponse(
                tx.getId(),
                tx.getFromAccount() != null ? tx.getFromAccount().getId() : null,
                tx.getToAccount() != null ? tx.getToAccount().getId() : null,
                tx.getAmount(),
                tx.getDescription(),
                tx.getCreatedAt()
        );
    }

    private TransactionReport toReport(Transaction tx) {
        Account from = tx.getFromAccount();
        Account to   = tx.getToAccount();
        return new TransactionReport(
                tx.getId(),
                from != null ? from.getAccountNumber() : null,
                from != null ? from.getOwner().getUsername() : null,
                to   != null ? to.getAccountNumber()   : null,
                to   != null ? to.getOwner().getUsername()   : null,
                tx.getAmount(),
                tx.getDescription(),
                tx.getCreatedAt()
        );
    }
}
