package com.melof10.santander.service.impl;

import com.melof10.santander.controller.request.CustomerCreateRequest;
import com.melof10.santander.controller.request.CustomerUpdateRequest;
import com.melof10.santander.entity.Customer;
import com.melof10.santander.exception.DuplicateResourceException;
import com.melof10.santander.exception.NotFoundException;
import com.melof10.santander.repository.CustomerRepository;
import com.melof10.santander.service.ICustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerServiceImpl implements ICustomerService {

    private final CustomerRepository customerRepository;

    @Override
    public Customer create(CustomerCreateRequest req) {
        if (customerRepository.existsByDocument(req.getDocument())) {
            throw new DuplicateResourceException("El documento ya existe: " + req.getDocument());
        }
        Customer c = Customer.builder()
                .firstName(req.getFirstName())
                .lastName(req.getLastName())
                .document(req.getDocument())
                .email(req.getEmail())
                .phone(req.getPhone())
                .build();
        return customerRepository.save(c);
    }

    @Override
    public Customer update(Long id, CustomerUpdateRequest req) {
        Customer c = customerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Cliente no encontrado: " + id));
        c.setFirstName(req.getFirstName());
        c.setLastName(req.getLastName());
        c.setEmail(req.getEmail());
        c.setPhone(req.getPhone());
        return customerRepository.save(c);
    }

    @Override
    public void delete(Long id) {
        if (!customerRepository.existsById(id)) {
            throw new NotFoundException("Cliente no encontrado: " + id);
        }
        customerRepository.deleteById(id);
    }

    @Override @Transactional(readOnly = true)
    public Customer getById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Cliente no encontrado: " + id));
    }

    @Override @Transactional(readOnly = true)
    public List<Customer> getAll() {
        return customerRepository.findAll();
    }

    @Override @Transactional(readOnly = true)
    public Customer getByDocument(String document) {
        return customerRepository.findByDocument(document)
                .orElseThrow(() -> new NotFoundException("Cliente no encontrado con documento: " + document));
    }
}

