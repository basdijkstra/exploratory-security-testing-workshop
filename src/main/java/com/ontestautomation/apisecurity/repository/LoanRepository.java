package com.ontestautomation.apisecurity.repository;

import com.ontestautomation.apisecurity.model.Loan;
import com.ontestautomation.apisecurity.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, Long> {
    List<Loan> findByApplicant(User applicant);
}
