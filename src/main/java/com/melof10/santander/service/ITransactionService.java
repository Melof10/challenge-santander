package com.melof10.santander.service;

import com.melof10.santander.controller.request.TransactionCreateRequest;
import com.melof10.santander.controller.request.TransferRequest;
import com.melof10.santander.entity.Transaction;

import java.util.List;

public interface ITransactionService {

    Transaction create(TransactionCreateRequest req);
    Transaction transfer(TransferRequest req);
    Transaction getById(Long id);
    List<Transaction> getAll();
    void delete(Long id);
}

