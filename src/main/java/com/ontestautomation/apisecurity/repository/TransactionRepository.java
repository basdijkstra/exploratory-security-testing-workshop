package com.ontestautomation.apisecurity.repository;

import com.ontestautomation.apisecurity.model.Transaction;
import com.ontestautomation.apisecurity.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, String> {

    @Query("SELECT t FROM Transaction t WHERE " +
           "(t.fromAccount IS NOT NULL AND t.fromAccount.owner = :user) OR " +
           "(t.toAccount IS NOT NULL AND t.toAccount.owner = :user)")
    List<Transaction> findByUser(@Param("user") User user);
}
