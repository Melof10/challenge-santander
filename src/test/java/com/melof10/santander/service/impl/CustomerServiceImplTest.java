package com.melof10.santander.service.impl;

import com.melof10.santander.controller.request.CustomerCreateRequest;
import com.melof10.santander.controller.request.CustomerUpdateRequest;
import com.melof10.santander.entity.Customer;
import com.melof10.santander.exception.DuplicateResourceException;
import com.melof10.santander.exception.NotFoundException;
import com.melof10.santander.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    private CustomerServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new CustomerServiceImpl(customerRepository);
    }

    @Test
    void create_shouldPersistCustomer_whenValid() {
        CustomerCreateRequest req = new CustomerCreateRequest();
        req.setFirstName("Juan");
        req.setLastName("Pérez");
        req.setDocument("30111222");
        req.setEmail("juan.perez@santander.test");
        req.setPhone("+54-11-5555-1001");

        when(customerRepository.existsByDocument("30111222")).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> {
            Customer c = inv.getArgument(0);
            c.setId(1L);
            return c;
        });

        Customer saved = service.create(req);

        assertThat(saved.getId()).isEqualTo(1L);
        assertThat(saved.getFirstName()).isEqualTo("Juan");
        assertThat(saved.getLastName()).isEqualTo("Pérez");
        assertThat(saved.getDocument()).isEqualTo("30111222");
        assertThat(saved.getEmail()).isEqualTo("juan.perez@santander.test");
        assertThat(saved.getPhone()).isEqualTo("+54-11-5555-1001");

        verify(customerRepository).existsByDocument("30111222");
        verify(customerRepository).save(argThat(c ->
                "Juan".equals(c.getFirstName()) &&
                        "Pérez".equals(c.getLastName()) &&
                        "30111222".equals(c.getDocument()) &&
                        "juan.perez@santander.test".equals(c.getEmail()) &&
                        "+54-11-5555-1001".equals(c.getPhone())
        ));
    }

    @Test
    void create_shouldThrowDuplicate_whenDocumentExists() {
        CustomerCreateRequest req = new CustomerCreateRequest();
        req.setDocument("30111222");

        when(customerRepository.existsByDocument("30111222")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> service.create(req));

        verify(customerRepository).existsByDocument("30111222");
        verify(customerRepository, never()).save(any());
    }

    @Test
    void update_shouldModifyFields_whenCustomerExists() {
        Customer existing = Customer.builder()
                .id(5L)
                .firstName("Juan")
                .lastName("Pérez")
                .document("30111222")
                .email("old@mail.test")
                .phone("111")
                .build();

        CustomerUpdateRequest req = new CustomerUpdateRequest();
        req.setFirstName("Juana");
        req.setLastName("García");
        req.setEmail("juana.garcia@santander.test");
        req.setPhone("222");

        when(customerRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));

        Customer updated = service.update(5L, req);

        assertThat(updated.getFirstName()).isEqualTo("Juana");
        assertThat(updated.getLastName()).isEqualTo("García");
        assertThat(updated.getEmail()).isEqualTo("juana.garcia@santander.test");
        assertThat(updated.getPhone()).isEqualTo("222");

        verify(customerRepository).findById(5L);
        verify(customerRepository).save(same(existing));
    }

    @Test
    void update_shouldThrowNotFound_whenCustomerMissing() {
        CustomerUpdateRequest req = new CustomerUpdateRequest();
        when(customerRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.update(404L, req));

        verify(customerRepository).findById(404L);
        verify(customerRepository, never()).save(any());
    }

    @Test
    void delete_shouldRemove_whenExists() {
        when(customerRepository.existsById(7L)).thenReturn(true);

        service.delete(7L);

        verify(customerRepository).existsById(7L);
        verify(customerRepository).deleteById(7L);
    }

    @Test
    void delete_shouldThrowNotFound_whenMissing() {
        when(customerRepository.existsById(77L)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> service.delete(77L));

        verify(customerRepository).existsById(77L);
        verify(customerRepository, never()).deleteById(anyLong());
    }

    @Test
    void getById_shouldReturn_whenExists() {
        Customer c = Customer.builder().id(3L).firstName("A").lastName("B").document("X").build();
        when(customerRepository.findById(3L)).thenReturn(Optional.of(c));

        Customer found = service.getById(3L);

        assertThat(found.getId()).isEqualTo(3L);
        assertThat(found.getFirstName()).isEqualTo("A");

        verify(customerRepository).findById(3L);
    }

    @Test
    void getById_shouldThrowNotFound_whenMissing() {
        when(customerRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.getById(999L));

        verify(customerRepository).findById(999L);
    }

    @Test
    void getAll_shouldReturnListFromRepository() {
        when(customerRepository.findAll()).thenReturn(
                List.of(
                        Customer.builder().id(1L).document("D1").build(),
                        Customer.builder().id(2L).document("D2").build()
                )
        );

        List<Customer> all = service.getAll();

        assertThat(all).hasSize(2);
        assertThat(all.get(0).getId()).isEqualTo(1L);

        verify(customerRepository).findAll();
    }

    @Test
    void getByDocument_shouldReturn_whenExists() {
        Customer c = Customer.builder().id(10L).document("ABC123").build();
        when(customerRepository.findByDocument("ABC123")).thenReturn(Optional.of(c));

        Customer found = service.getByDocument("ABC123");

        assertThat(found.getId()).isEqualTo(10L);
        assertThat(found.getDocument()).isEqualTo("ABC123");

        verify(customerRepository).findByDocument("ABC123");
    }

    @Test
    void getByDocument_shouldThrowNotFound_whenMissing() {
        when(customerRepository.findByDocument("NOPE")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.getByDocument("NOPE"));

        verify(customerRepository).findByDocument("NOPE");
    }
}

