package com.ontestautomation.apisecurity.config;

import com.ontestautomation.apisecurity.model.*;
import com.ontestautomation.apisecurity.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DataSeeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final LoanRepository loanRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        seedUsers();
        seedAccounts();
        seedTransactions();
        seedLoans();
        printCredentials();
    }

    private void seedUsers() {
        userRepository.save(User.builder()
                .username("alice")
                .email("alice@bank.example")
                .passwordHash(passwordEncoder.encode("alice123"))
                .role(Role.CUSTOMER)
                .build());

        userRepository.save(User.builder()
                .username("bob")
                .email("bob@bank.example")
                .passwordHash(passwordEncoder.encode("bob123"))
                .role(Role.CUSTOMER)
                .build());

        userRepository.save(User.builder()
                .username("admin")
                .email("admin@bank.example")
                .passwordHash(passwordEncoder.encode("admin123"))
                .role(Role.ADMIN)
                .build());
    }

    private void seedAccounts() {
        User alice = userRepository.findByUsername("alice").orElseThrow();
        User bob   = userRepository.findByUsername("bob").orElseThrow();

        // Alice accounts
        accountRepository.save(Account.builder()
                .id("account-714")
                .accountNumber("NL10BANK0000000001")
                .type(AccountType.CHECKING)
                .balance(new BigDecimal("2500.00"))
                .owner(alice)
                .build());

        accountRepository.save(Account.builder()
                .id("account-293")
                .accountNumber("NL10BANK0000000002")
                .type(AccountType.SAVINGS)
                .balance(new BigDecimal("12000.00"))
                .owner(alice)
                .build());

        // Bob accounts
        accountRepository.save(Account.builder()
                .id("account-581")
                .accountNumber("NL10BANK0000000003")
                .type(AccountType.CHECKING)
                .balance(new BigDecimal("1800.00"))
                .owner(bob)
                .build());

        accountRepository.save(Account.builder()
                .id("account-847")
                .accountNumber("NL10BANK0000000004")
                .type(AccountType.SAVINGS)
                .balance(new BigDecimal("5500.00"))
                .owner(bob)
                .build());

        // Payroll account owned by admin
        User admin = userRepository.findByUsername("admin").orElseThrow();
        accountRepository.save(Account.builder()
                .id("account-036")
                .accountNumber("NL10BANK0000000005")
                .type(AccountType.CHECKING)
                .balance(new BigDecimal("999999.00"))
                .owner(admin)
                .build());
    }

    private void seedTransactions() {
        Account aliceChecking = accountRepository.findById("account-714").orElseThrow();
        Account aliceSavings  = accountRepository.findById("account-293").orElseThrow();
        Account bobChecking   = accountRepository.findById("account-581").orElseThrow();
        Account bobSavings    = accountRepository.findById("account-847").orElseThrow();
        Account payroll       = accountRepository.findById("account-036").orElseThrow();

        LocalDateTime base = LocalDateTime.now().minusDays(30);

        transactionRepository.save(Transaction.builder()
                .id("txn-492")
                .fromAccount(payroll)
                .toAccount(aliceChecking)
                .amount(new BigDecimal("3000.00"))
                .description("Salary deposit")
                .createdAt(base)
                .build());

        transactionRepository.save(Transaction.builder()
                .id("txn-831")
                .fromAccount(aliceChecking)
                .toAccount(aliceSavings)
                .amount(new BigDecimal("500.00"))
                .description("Monthly savings transfer")
                .createdAt(base.plusDays(1))
                .build());

        transactionRepository.save(Transaction.builder()
                .id("txn-267")
                .fromAccount(aliceChecking)
                .toAccount(bobChecking)
                .amount(new BigDecimal("120.00"))
                .description("Dinner split")
                .createdAt(base.plusDays(3))
                .build());

        transactionRepository.save(Transaction.builder()
                .id("txn-154")
                .fromAccount(payroll)
                .toAccount(bobChecking)
                .amount(new BigDecimal("3000.00"))
                .description("Salary deposit")
                .createdAt(base.plusDays(5))
                .build());

        transactionRepository.save(Transaction.builder()
                .id("txn-739")
                .fromAccount(bobChecking)
                .toAccount(bobSavings)
                .amount(new BigDecimal("300.00"))
                .description("Monthly savings transfer")
                .createdAt(base.plusDays(6))
                .build());

        transactionRepository.save(Transaction.builder()
                .id("txn-318")
                .fromAccount(aliceChecking)
                .toAccount(bobChecking)
                .amount(new BigDecimal("75.00"))
                .description("Concert tickets")
                .createdAt(base.plusDays(10))
                .build());

        transactionRepository.save(Transaction.builder()
                .id("txn-625")
                .fromAccount(bobChecking)
                .toAccount(aliceChecking)
                .amount(new BigDecimal("50.00"))
                .description("Lunch refund")
                .createdAt(base.plusDays(14))
                .build());

        transactionRepository.save(Transaction.builder()
                .id("txn-943")
                .fromAccount(aliceChecking)
                .toAccount(null)
                .amount(new BigDecimal("200.00"))
                .description("ATM cash withdrawal")
                .createdAt(base.plusDays(18))
                .build());

        transactionRepository.save(Transaction.builder()
                .id("txn-076")
                .fromAccount(bobChecking)
                .toAccount(aliceChecking)
                .amount(new BigDecimal("350.00"))
                .description("Rent contribution")
                .createdAt(base.plusDays(22))
                .build());

        transactionRepository.save(Transaction.builder()
                .id("txn-512")
                .fromAccount(aliceSavings)
                .toAccount(aliceChecking)
                .amount(new BigDecimal("1000.00"))
                .description("Holiday fund withdrawal")
                .createdAt(base.plusDays(25))
                .build());
    }

    private void seedLoans() {
        User alice = userRepository.findByUsername("alice").orElseThrow();
        User bob   = userRepository.findByUsername("bob").orElseThrow();

        loanRepository.save(Loan.builder()
                .id("loan-283")
                .applicant(alice)
                .amount(new BigDecimal("5000.00"))
                .status(LoanStatus.PENDING)
                .requestedAt(LocalDateTime.now().minusDays(5))
                .build());

        loanRepository.save(Loan.builder()
                .id("loan-617")
                .applicant(bob)
                .amount(new BigDecimal("15000.00"))
                .status(LoanStatus.APPROVED)
                .requestedAt(LocalDateTime.now().minusDays(20))
                .build());
    }

    private void printCredentials() {
        System.out.println("""

                ╔══════════════════════════════════════════════════════════╗
                ║                WORKSHOP SEED CREDENTIALS                 ║
                ╠══════════════════════════════════════════════════════════╣
                ║  alice  / alice123  (CUSTOMER)                           ║
                ║  bob    / bob123    (CUSTOMER)                           ║
                ║  admin  / admin123  (ADMIN)                              ║
                ╠══════════════════════════════════════════════════════════╣
                ║  Swagger:    http://localhost:8080/swagger-ui/index.html ║
                ║  H2 console: http://localhost:8080/h2-console            ║
                ║  JDBC URL:   jdbc:h2:mem:bankdb                          ║
                ╚══════════════════════════════════════════════════════════╝
                """);
    }
}
