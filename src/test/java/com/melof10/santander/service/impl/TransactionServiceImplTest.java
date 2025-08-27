package com.melof10.santander.service.impl;

import com.melof10.santander.controller.request.TransactionCreateRequest;
import com.melof10.santander.controller.request.TransferRequest;
import com.melof10.santander.entity.Account;
import com.melof10.santander.entity.Transaction;
import com.melof10.santander.enums.TransactionType;
import com.melof10.santander.exception.BusinessException;
import com.melof10.santander.exception.NotFoundException;
import com.melof10.santander.repository.AccountRepository;
import com.melof10.santander.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock private TransactionRepository transactionRepository;
    @Mock private AccountRepository accountRepository;

    private TransactionServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new TransactionServiceImpl(transactionRepository, accountRepository);
    }

    @Test
    void create_deposito_shouldPersist_whenValid() {
        Account dest = Account.builder().id(10L).balance(new BigDecimal("100.00")).build();

        TransactionCreateRequest req = new TransactionCreateRequest();
        req.setType(TransactionType.DEPOSITO);
        req.setDestinationAccountId(10L);
        req.setAmount(new BigDecimal("50.00"));

        when(accountRepository.findById(10L)).thenReturn(Optional.of(dest));
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> {
            Transaction t = inv.getArgument(0);
            t.setId(1L);
            return t;
        });

        Transaction tx = service.create(req);

        assertThat(dest.getBalance()).isEqualByComparingTo("150.00");
        assertThat(tx.getId()).isEqualTo(1L);
        assertThat(tx.getType()).isEqualTo(TransactionType.DEPOSITO);
        assertThat(tx.getSourceAccount()).isNull();
        assertThat(tx.getDestinationAccount()).isSameAs(dest);
        assertThat(tx.getAmount()).isEqualByComparingTo("50.00");

        verify(accountRepository).findById(10L);
        verify(accountRepository).save(same(dest));
        verify(transactionRepository).save(argThat(t ->
                t.getType() == TransactionType.DEPOSITO &&
                        t.getDestinationAccount() == dest &&
                        t.getAmount().compareTo(new BigDecimal("50.00")) == 0
        ));
    }

    @Test
    void create_deposito_shouldThrow_whenDestinationMissing() {
        TransactionCreateRequest req = new TransactionCreateRequest();
        req.setType(TransactionType.DEPOSITO);
        req.setAmount(new BigDecimal("10"));

        BusinessException ex = assertThrows(BusinessException.class, () -> service.create(req));
        assertThat(ex.getMessage()).contains("cuenta destino requerida");
        verify(accountRepository, never()).findById(anyLong());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void create_deposito_shouldThrow_whenDestinationNotFound() {
        TransactionCreateRequest req = new TransactionCreateRequest();
        req.setType(TransactionType.DEPOSITO);
        req.setDestinationAccountId(99L);
        req.setAmount(new BigDecimal("10"));

        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.create(req));
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void create_deposito_shouldThrow_whenAmountInvalid() {
        Account dest = Account.builder().id(10L).balance(new BigDecimal("100")).build();

        TransactionCreateRequest req = new TransactionCreateRequest();
        req.setType(TransactionType.DEPOSITO);
        req.setDestinationAccountId(10L);
        req.setAmount(new BigDecimal("0"));

        when(accountRepository.findById(10L)).thenReturn(Optional.of(dest));

        BusinessException ex = assertThrows(BusinessException.class, () -> service.create(req));
        assertThat(ex.getMessage()).contains("Monto inválido");
        verify(accountRepository, never()).save(any(Account.class));
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void create_extraccion_shouldPersist_whenValid() {
        Account source = Account.builder().id(7L).balance(new BigDecimal("200.00")).build();

        TransactionCreateRequest req = new TransactionCreateRequest();
        req.setType(TransactionType.EXTRACCION);
        req.setSourceAccountId(7L);
        req.setAmount(new BigDecimal("50.00"));

        when(accountRepository.findById(7L)).thenReturn(Optional.of(source));
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> {
            Transaction t = inv.getArgument(0);
            t.setId(2L);
            return t;
        });

        Transaction tx = service.create(req);

        assertThat(source.getBalance()).isEqualByComparingTo("150.00");
        assertThat(tx.getId()).isEqualTo(2L);
        assertThat(tx.getType()).isEqualTo(TransactionType.EXTRACCION);
        assertThat(tx.getSourceAccount()).isSameAs(source);
        assertThat(tx.getDestinationAccount()).isNull();
        assertThat(tx.getAmount()).isEqualByComparingTo("50.00");

        verify(accountRepository).save(same(source));
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void create_extraccion_shouldThrow_whenSourceMissing() {
        TransactionCreateRequest req = new TransactionCreateRequest();
        req.setType(TransactionType.EXTRACCION);
        req.setAmount(new BigDecimal("10"));

        BusinessException ex = assertThrows(BusinessException.class, () -> service.create(req));
        assertThat(ex.getMessage()).contains("cuenta origen requerida");
        verify(accountRepository, never()).findById(anyLong());
    }

    @Test
    void create_extraccion_shouldThrow_whenSourceNotFound() {
        TransactionCreateRequest req = new TransactionCreateRequest();
        req.setType(TransactionType.EXTRACCION);
        req.setSourceAccountId(77L);
        req.setAmount(new BigDecimal("10"));

        when(accountRepository.findById(77L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.create(req));
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void create_extraccion_shouldThrow_whenBalanceNullOrZero() {
        Account source = Account.builder().id(1L).balance(null).build();

        TransactionCreateRequest req = new TransactionCreateRequest();
        req.setType(TransactionType.EXTRACCION);
        req.setSourceAccountId(1L);
        req.setAmount(new BigDecimal("5"));

        when(accountRepository.findById(1L)).thenReturn(Optional.of(source));

        BusinessException ex = assertThrows(BusinessException.class, () -> service.create(req));
        assertThat(ex.getMessage()).contains("No tiene más dinero en la cuenta");
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void create_extraccion_shouldThrow_whenInsufficientFunds() {
        Account source = Account.builder().id(1L).balance(new BigDecimal("10.00")).build();

        TransactionCreateRequest req = new TransactionCreateRequest();
        req.setType(TransactionType.EXTRACCION);
        req.setSourceAccountId(1L);
        req.setAmount(new BigDecimal("20.00"));

        when(accountRepository.findById(1L)).thenReturn(Optional.of(source));

        BusinessException ex = assertThrows(BusinessException.class, () -> service.create(req));
        assertThat(ex.getMessage()).contains("Saldo insuficiente");
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void create_extraccion_shouldThrow_whenAmountInvalid() {
        Account source = Account.builder().id(7L).balance(new BigDecimal("100")).build();

        TransactionCreateRequest req = new TransactionCreateRequest();
        req.setType(TransactionType.EXTRACCION);
        req.setSourceAccountId(7L);
        req.setAmount(new BigDecimal("-1"));

        when(accountRepository.findById(7L)).thenReturn(Optional.of(source));

        BusinessException ex = assertThrows(BusinessException.class, () -> service.create(req));
        assertThat(ex.getMessage()).contains("Monto inválido");
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void create_shouldThrow_whenTypeNull() {
        TransactionCreateRequest req = new TransactionCreateRequest();
        req.setAmount(new BigDecimal("10"));

        assertThatThrownBy(() -> service.create(req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("no soportado");
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void create_shouldThrow_whenUnsupportedTransactionType_lenient() {
        TransactionCreateRequest req = mock(TransactionCreateRequest.class);
        TransactionType fakeType = mock(TransactionType.class);

        when(req.getType()).thenReturn(fakeType);
        lenient().when(req.getAmount()).thenReturn(new BigDecimal("10"));

        assertThrows(BusinessException.class, () -> service.create(req));
    }

    @Test
    void create_transfer_shouldDelegateToTransfer() {
        TransactionCreateRequest req = new TransactionCreateRequest();
        req.setType(TransactionType.TRANSFERENCIA);
        req.setSourceAccountId(1L);
        req.setDestinationAccountId(2L);
        req.setAmount(new BigDecimal("10"));

        Account source = Account.builder().id(1L).balance(new BigDecimal("50")).build();
        Account dest = Account.builder().id(2L).balance(new BigDecimal("5")).build();

        when(accountRepository.findById(1L)).thenReturn(Optional.of(source));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(dest));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> {
            Transaction t = inv.getArgument(0);
            t.setId(99L);
            return t;
        });

        Transaction tx = service.create(req);

        assertThat(source.getBalance()).isEqualByComparingTo("40");
        assertThat(dest.getBalance()).isEqualByComparingTo("15");
        assertThat(tx.getId()).isEqualTo(99L);
        assertThat(tx.getType()).isEqualTo(TransactionType.TRANSFERENCIA);
        assertThat(tx.getSourceAccount()).isSameAs(source);
        assertThat(tx.getDestinationAccount()).isSameAs(dest);
    }

    @Test
    void transfer_shouldThrow_whenSameAccounts() {
        TransferRequest req = new TransferRequest();
        req.setSourceAccountId(1L);
        req.setDestinationAccountId(1L);
        req.setAmount(new BigDecimal("10"));

        BusinessException ex = assertThrows(BusinessException.class, () -> service.transfer(req));
        assertThat(ex.getMessage()).contains("cuentas iguales");
    }

    @Test
    void transfer_shouldThrow_whenSourceNotFound() {
        TransferRequest req = new TransferRequest();
        req.setSourceAccountId(1L);
        req.setDestinationAccountId(2L);
        req.setAmount(new BigDecimal("10"));

        when(accountRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.transfer(req));
    }

    @Test
    void transfer_shouldThrow_whenDestinationNotFound() {
        TransferRequest req = new TransferRequest();
        req.setSourceAccountId(1L);
        req.setDestinationAccountId(2L);
        req.setAmount(new BigDecimal("10"));

        when(accountRepository.findById(1L)).thenReturn(Optional.of(Account.builder().id(1L).balance(new BigDecimal("100")).build()));
        when(accountRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.transfer(req));
    }

    @Test
    void transfer_shouldThrow_whenAmountInvalid() {
        TransferRequest req = new TransferRequest();
        req.setSourceAccountId(1L);
        req.setDestinationAccountId(2L);
        req.setAmount(new BigDecimal("0"));

        when(accountRepository.findById(1L)).thenReturn(Optional.of(Account.builder().id(1L).balance(new BigDecimal("10")).build()));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(Account.builder().id(2L).balance(new BigDecimal("5")).build()));

        BusinessException ex = assertThrows(BusinessException.class, () -> service.transfer(req));
        assertThat(ex.getMessage()).contains("Monto inválido");
        verify(accountRepository, never()).save(any(Account.class));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void transfer_shouldWithdrawAndDepositAndPersist_whenValid() {
        Account source = Account.builder().id(1L).balance(new BigDecimal("100.00")).build();
        Account dest = Account.builder().id(2L).balance(new BigDecimal("10.00")).build();

        TransferRequest req = new TransferRequest();
        req.setSourceAccountId(1L);
        req.setDestinationAccountId(2L);
        req.setAmount(new BigDecimal("25.50"));

        when(accountRepository.findById(1L)).thenReturn(Optional.of(source));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(dest));
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> {
            Transaction t = inv.getArgument(0);
            t.setId(7L);
            return t;
        });

        Transaction tx = service.transfer(req);

        assertThat(source.getBalance()).isEqualByComparingTo("74.50");
        assertThat(dest.getBalance()).isEqualByComparingTo("35.50");
        assertThat(tx.getId()).isEqualTo(7L);
        assertThat(tx.getType()).isEqualTo(TransactionType.TRANSFERENCIA);
        assertThat(tx.getSourceAccount()).isSameAs(source);
        assertThat(tx.getDestinationAccount()).isSameAs(dest);

        verify(accountRepository, times(2)).save(any(Account.class));
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void getById_shouldReturn_whenExists() {
        Transaction t = Transaction.builder().id(3L).type(TransactionType.DEPOSITO).build();
        when(transactionRepository.findById(3L)).thenReturn(Optional.of(t));

        Transaction found = service.getById(3L);

        assertThat(found.getId()).isEqualTo(3L);
        verify(transactionRepository).findById(3L);
    }

    @Test
    void getById_shouldThrow_whenMissing() {
        when(transactionRepository.findById(404L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.getById(404L));
    }

    @Test
    void getAll_shouldUseCustomQuery() {
        when(transactionRepository.findAllWithAccounts()).thenReturn(
                List.of(Transaction.builder().id(1L).build(), Transaction.builder().id(2L).build())
        );

        List<Transaction> all = service.getAll();

        assertThat(all).hasSize(2);
        verify(transactionRepository).findAllWithAccounts();
    }

    @Test
    void delete_shouldRemove_whenExists() {
        when(transactionRepository.existsById(9L)).thenReturn(true);

        service.delete(9L);

        verify(transactionRepository).existsById(9L);
        verify(transactionRepository).deleteById(9L);
    }

    @Test
    void delete_shouldThrow_whenMissing() {
        when(transactionRepository.existsById(9L)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> service.delete(9L));

        verify(transactionRepository).existsById(9L);
        verify(transactionRepository, never()).deleteById(anyLong());
    }
}

