package com.melof10.santander.controller.rest;

import com.melof10.santander.controller.request.AccountCreateRequest;
import com.melof10.santander.controller.request.AccountUpdateRequest;
import com.melof10.santander.entity.Account;
import com.melof10.santander.service.IAccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final IAccountService service;

    @PostMapping
    public ResponseEntity<Account> create(@Valid @RequestBody AccountCreateRequest req) {
        Account saved = service.create(req);
        return ResponseEntity.created(URI.create("/api/accounts/" + saved.getId())).body(saved);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Account> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<Account>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Account> update(@PathVariable Long id, @Valid @RequestBody AccountUpdateRequest req) {
        return ResponseEntity.ok(service.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/by-customer/{customerId}")
    public ResponseEntity<List<Account>> getByCustomer(@PathVariable Long customerId) {
        return ResponseEntity.ok(service.getByCustomerId(customerId));
    }

    @GetMapping("/self/{id}")
    public ResponseEntity<Account> selfCall(@PathVariable Long id) {
        return ResponseEntity.ok(service.selfGet(id));
    }
}
