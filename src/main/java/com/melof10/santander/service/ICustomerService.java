package com.melof10.santander.service;

import com.melof10.santander.controller.request.CustomerCreateRequest;
import com.melof10.santander.controller.request.CustomerUpdateRequest;
import com.melof10.santander.entity.Customer;

import java.util.List;

public interface ICustomerService {

    Customer create(CustomerCreateRequest req);
    Customer update(Long id, CustomerUpdateRequest req);
    void delete(Long id);
    Customer getById(Long id);
    List<Customer> getAll();
    Customer getByDocument(String document);
}

