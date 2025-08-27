package com.melof10.santander.repository;

import com.melof10.santander.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByDocument(String document);

    boolean existsByDocument(String document);

    Page<Customer> findByLastNameContainingIgnoreCase(String lastName, Pageable pageable);
}

