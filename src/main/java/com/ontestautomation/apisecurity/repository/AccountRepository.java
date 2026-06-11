package com.ontestautomation.apisecurity.repository;

import com.ontestautomation.apisecurity.model.Account;
import com.ontestautomation.apisecurity.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountRepository extends JpaRepository<Account, String> {
    List<Account> findByOwner(User owner);
}
