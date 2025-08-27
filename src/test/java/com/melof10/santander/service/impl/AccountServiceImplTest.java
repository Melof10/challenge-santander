package com.melof10.santander.service.impl;

import com.melof10.santander.client.AccountClient;
import com.melof10.santander.controller.request.AccountCreateRequest;
import com.melof10.santander.controller.request.AccountUpdateRequest;
import com.melof10.santander.entity.Account;
import com.melof10.santander.entity.Customer;
import com.melof10.santander.enums.AccountType;
import com.melof10.santander.exception.DuplicateResourceException;
import com.melof10.santander.exception.NotFoundException;
import com.melof10.santander.repository.AccountRepository;
import com.melof10.santander.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    @Mock private AccountRepository accountRepository;
    @Mock private CustomerRepository customerRepository;
    @Mock private AccountClient accountClient;

    private AccountServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AccountServiceImpl(accountRepository, customerRepository, accountClient);
    }

    @Test
    void create_shouldPersistAccount_whenValid() {
        AccountCreateRequest req = new AccountCreateRequest();
        req.setAccountNumber("ACC-1001");
        req.setAccountType(AccountType.CAJA_AHORRO);
        req.setInitialBalance(new BigDecimal("500.00"));
        req.setCustomerId(10L);

        Customer customer = Customer.builder().id(10L).firstName("Juan").lastName("Pérez").build();

        when(accountRepository.existsByAccountNumber("ACC-1001")).thenReturn(false);
        when(customerRepository.findById(10L)).thenReturn(Optional.of(customer));
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> {
            Account a = inv.getArgument(0);
            a.setId(1L);
            return a;
        });

        Account saved = service.create(req);

        assertThat(saved.getId()).isEqualTo(1L);
        assertThat(saved.getAccountNumber()).isEqualTo("ACC-1001");
        assertThat(saved.getAccountType()).isEqualTo(AccountType.CAJA_AHORRO);
        assertThat(saved.getBalance()).isEqualByComparingTo("500.00");
        assertThat(saved.getCustomer().getId()).isEqualTo(10L);

        verify(accountRepository).existsByAccountNumber("ACC-1001");
        verify(customerRepository).findById(10L);
        verify(accountRepository).save(argThat(a ->
                "ACC-1001".equals(a.getAccountNumber())
                        && a.getAccountType() == AccountType.CAJA_AHORRO
                        && a.getBalance().compareTo(new BigDecimal("500.00")) == 0
                        && a.getCustomer() == customer
        ));
    }

    @Test
    void create_shouldThrowDuplicate_whenAccountNumberAlreadyExists() {
        AccountCreateRequest req = new AccountCreateRequest();
        req.setAccountNumber("DUP-001");

        when(accountRepository.existsByAccountNumber("DUP-001")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> service.create(req));

        verify(accountRepository).existsByAccountNumber("DUP-001");
        verify(customerRepository, never()).findById(anyLong());
        verify(accountRepository, never()).save(any());
    }

    @Test
    void create_shouldThrowNotFound_whenCustomerMissing() {
        AccountCreateRequest req = new AccountCreateRequest();
        req.setAccountNumber("ACC-2001");
        req.setAccountType(AccountType.CUENTA_CORRIENTE);
        req.setInitialBalance(new BigDecimal("100.00"));
        req.setCustomerId(99L);

        when(accountRepository.existsByAccountNumber("ACC-2001")).thenReturn(false);
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.create(req));

        verify(accountRepository).existsByAccountNumber("ACC-2001");
        verify(customerRepository).findById(99L);
        verify(accountRepository, never()).save(any());
    }

    @Test
    void update_shouldChangeTypeAndBalance_whenExists() {
        Account existing = Account.builder()
                .id(3L)
                .accountNumber("ACC-3")
                .accountType(AccountType.CAJA_AHORRO)
                .balance(new BigDecimal("50.00"))
                .build();

        AccountUpdateRequest req = new AccountUpdateRequest();
        req.setAccountType(AccountType.CUENTA_CORRIENTE);
        req.setBalance(new BigDecimal("75.55"));

        when(accountRepository.findById(3L)).thenReturn(Optional.of(existing));
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        Account updated = service.update(3L, req);

        assertThat(updated.getAccountType()).isEqualTo(AccountType.CUENTA_CORRIENTE);
        assertThat(updated.getBalance()).isEqualByComparingTo("75.55");

        verify(accountRepository).findById(3L);
        verify(accountRepository).save(same(existing));
    }

    @Test
    void update_shouldThrowNotFound_whenMissing() {
        AccountUpdateRequest req = new AccountUpdateRequest();
        when(accountRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.update(404L, req));

        verify(accountRepository).findById(404L);
        verify(accountRepository, never()).save(any());
    }

    @Test
    void delete_shouldRemove_whenExists() {
        when(accountRepository.existsById(5L)).thenReturn(true);

        service.delete(5L);

        verify(accountRepository).existsById(5L);
        verify(accountRepository).deleteById(5L);
    }

    @Test
    void delete_shouldThrowNotFound_whenMissing() {
        when(accountRepository.existsById(77L)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> service.delete(77L));

        verify(accountRepository).existsById(77L);
        verify(accountRepository, never()).deleteById(anyLong());
    }

    @Test
    void getById_shouldReturn_whenExists() {
        Account a = Account.builder().id(8L).accountNumber("ACC-8").build();
        when(accountRepository.findById(8L)).thenReturn(Optional.of(a));

        Account result = service.getById(8L);

        assertThat(result.getId()).isEqualTo(8L);
        assertThat(result.getAccountNumber()).isEqualTo("ACC-8");

        verify(accountRepository).findById(8L);
    }

    @Test
    void getById_shouldThrowNotFound_whenMissing() {
        when(accountRepository.findById(123L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.getById(123L));

        verify(accountRepository).findById(123L);
    }

    @Test
    void getAll_shouldReturnListFromRepository() {
        when(accountRepository.findAll()).thenReturn(
                List.of(Account.builder().id(1L).build(), Account.builder().id(2L).build())
        );

        List<Account> all = service.getAll();

        assertThat(all).hasSize(2);
        assertThat(all.get(0).getId()).isEqualTo(1L);

        verify(accountRepository).findAll();
    }

    @Test
    void getByCustomerId_shouldReturnListForCustomer() {
        when(accountRepository.findByCustomer_Id(10L)).thenReturn(
                List.of(Account.builder().id(11L).build())
        );

        List<Account> list = service.getByCustomerId(10L);

        assertThat(list).hasSize(1);
        assertThat(list.get(0).getId()).isEqualTo(11L);

        verify(accountRepository).findByCustomer_Id(10L);
    }

    @Test
    void selfGet_shouldReturn_whenClientReturnsAccount() {
        Account a = Account.builder().id(9L).accountNumber("ACC-9").build();
        when(accountClient.getAccountById(9L)).thenReturn(Optional.of(a));

        Account result = service.selfGet(9L);

        assertThat(result.getId()).isEqualTo(9L);
        assertThat(result.getAccountNumber()).isEqualTo("ACC-9");

        verify(accountClient).getAccountById(9L);
    }

    @Test
    void selfGet_shouldThrowNotFound_whenClientEmpty() {
        when(accountClient.getAccountById(321L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.selfGet(321L));

        verify(accountClient).getAccountById(321L);
    }
}
