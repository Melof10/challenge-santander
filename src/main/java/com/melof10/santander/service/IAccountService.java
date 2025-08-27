package com.melof10.santander.service;

import com.melof10.santander.controller.request.AccountCreateRequest;
import com.melof10.santander.controller.request.AccountUpdateRequest;
import com.melof10.santander.entity.Account;

import java.util.List;

public interface IAccountService {

    Account create(AccountCreateRequest req);
    Account update(Long id, AccountUpdateRequest req);
    void delete(Long id);
    Account getById(Long id);
    List<Account> getAll();
    List<Account> getByCustomerId(Long customerId);
    Account selfGet(Long id);
}

