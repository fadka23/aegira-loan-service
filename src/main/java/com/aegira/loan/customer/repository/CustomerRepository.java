package com.aegira.loan.customer.repository;

import com.aegira.loan.customer.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    boolean existsByNik(String nik);
}
