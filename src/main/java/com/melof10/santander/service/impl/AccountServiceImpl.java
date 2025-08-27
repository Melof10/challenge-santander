package com.melof10.santander.service.impl;

import com.melof10.santander.client.AccountClient;
import com.melof10.santander.controller.request.AccountCreateRequest;
import com.melof10.santander.controller.request.AccountUpdateRequest;
import com.melof10.santander.entity.Account;
import com.melof10.santander.entity.Customer;
import com.melof10.santander.exception.DuplicateResourceException;
import com.melof10.santander.exception.NotFoundException;
import com.melof10.santander.repository.AccountRepository;
import com.melof10.santander.repository.CustomerRepository;
import com.melof10.santander.service.IAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AccountServiceImpl implements IAccountService {

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final AccountClient accountClient;

    @Override
    public Account create(AccountCreateRequest req) {
        if (accountRepository.existsByAccountNumber(req.getAccountNumber())) {
            throw new DuplicateResourceException("La cuenta ya existe: " + req.getAccountNumber());
        }
        Customer customer = customerRepository.findById(req.getCustomerId())
                .orElseThrow(() -> new NotFoundException("Cliente no encontrado: " + req.getCustomerId()));

        Account a = Account.builder()
                .accountNumber(req.getAccountNumber())
                .accountType(req.getAccountType())
                .balance(req.getInitialBalance())
                .customer(customer)
                .build();
        return accountRepository.save(a);
    }

    @Override
    public Account update(Long id, AccountUpdateRequest req) {
        Account a = accountRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Cuenta no encontrada: " + id));
        a.setAccountType(req.getAccountType());
        a.setBalance(req.getBalance());
        return accountRepository.save(a);
    }

    @Override
    public void delete(Long id) {
        if (!accountRepository.existsById(id)) {
            throw new NotFoundException("Cuenta no encontrada: " + id);
        }
        accountRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Account getById(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Cuenta no encontrada: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Account> getAll() {
        return accountRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Account> getByCustomerId(Long customerId) {
        return accountRepository.findByCustomer_Id(customerId);
    }

    @Override
    @Transactional(readOnly = true)
    public Account selfGet(Long id) {
        return accountClient.getAccountById(id)
                .orElseThrow(() -> new NotFoundException("Cuenta no encontrada (self-call): " + id));
    }
}
