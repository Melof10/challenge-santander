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
import com.melof10.santander.service.ITransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TransactionServiceImpl implements ITransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    @Override
    public Transaction create(TransactionCreateRequest req) {
        if (req.getType() == null) {
            throw new BusinessException("Tipo de transacción no soportado");
        }

        final TransactionType type = req.getType();

        if (type != TransactionType.DEPOSITO
                && type != TransactionType.EXTRACCION
                && type != TransactionType.TRANSFERENCIA) {
            throw new BusinessException("Tipo de transacción no soportado");
        }

        if (type == TransactionType.TRANSFERENCIA) {
            TransferRequest tr = new TransferRequest();
            tr.setSourceAccountId(req.getSourceAccountId());
            tr.setDestinationAccountId(req.getDestinationAccountId());
            tr.setAmount(req.getAmount());
            return transfer(tr);
        }

        switch (type) {
            case DEPOSITO -> {
                if (req.getDestinationAccountId() == null)
                    throw new BusinessException("Depósito: cuenta destino requerida");
                Account dest = accountRepository.findById(req.getDestinationAccountId())
                        .orElseThrow(() -> new NotFoundException("Cuenta destino no encontrada"));
                applyDeposit(dest, req.getAmount());
                return saveTransaction(null, dest, req.getAmount(), TransactionType.DEPOSITO);
            }
            case EXTRACCION -> {
                if (req.getSourceAccountId() == null)
                    throw new BusinessException("Extracción: cuenta origen requerida");
                Account source = accountRepository.findById(req.getSourceAccountId())
                        .orElseThrow(() -> new NotFoundException("Cuenta origen no encontrada"));
                applyWithdraw(source, req.getAmount());
                return saveTransaction(source, null, req.getAmount(), TransactionType.EXTRACCION);
            }
            default -> throw new BusinessException("Tipo de transacción no soportado");
        }
    }

    @Override
    public Transaction transfer(TransferRequest req) {
        if (req.getSourceAccountId().equals(req.getDestinationAccountId())) {
            throw new BusinessException("Transferencia inválida: cuentas iguales");
        }

        Account source = accountRepository.findById(req.getSourceAccountId())
                .orElseThrow(() -> new NotFoundException("Cuenta origen no encontrada"));
        Account dest = accountRepository.findById(req.getDestinationAccountId())
                .orElseThrow(() -> new NotFoundException("Cuenta destino no encontrada"));

        applyWithdraw(source, req.getAmount());
        applyDeposit(dest, req.getAmount());

        return saveTransaction(source, dest, req.getAmount(), TransactionType.TRANSFERENCIA);
    }

    private void applyDeposit(Account dest, BigDecimal amount) {
        validateAmount(amount);
        dest.setBalance(dest.getBalance().add(amount));
        accountRepository.save(dest);
    }

    private void applyWithdraw(Account source, BigDecimal amount) {
        validateAmount(amount);

        BigDecimal balance = source.getBalance() == null ? BigDecimal.ZERO : source.getBalance();

        if (balance.signum() <= 0) {
            throw new BusinessException("No tiene más dinero en la cuenta");
        }

        if (balance.compareTo(amount) < 0) {
            throw new BusinessException("Saldo insuficiente");
        }

        source.setBalance(balance.subtract(amount));
        accountRepository.save(source);
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new BusinessException("Monto inválido");
        }
    }

    private Transaction saveTransaction(Account source, Account dest, BigDecimal amount, TransactionType type) {
        Transaction tx = Transaction.builder()
                .sourceAccount(source)
                .destinationAccount(dest)
                .amount(amount)
                .type(type)
                .build();
        return transactionRepository.save(tx);
    }

    @Override
    @Transactional(readOnly = true)
    public Transaction getById(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Transacción no encontrada: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Transaction> getAll() {
        return transactionRepository.findAllWithAccounts();
    }

    @Override
    public void delete(Long id) {
        if (!transactionRepository.existsById(id)) {
            throw new NotFoundException("Transacción no encontrada: " + id);
        }
        transactionRepository.deleteById(id);
    }
}
