package com.melof10.santander.service.impl;

import com.melof10.santander.controller.request.CardCreateRequest;
import com.melof10.santander.controller.request.CardUpdateRequest;
import com.melof10.santander.entity.Card;
import com.melof10.santander.entity.Customer;
import com.melof10.santander.enums.CardType;
import com.melof10.santander.exception.DuplicateResourceException;
import com.melof10.santander.exception.NotFoundException;
import com.melof10.santander.repository.CardRepository;
import com.melof10.santander.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceImplTest {

    @Mock private CardRepository cardRepository;
    @Mock private CustomerRepository customerRepository;

    private CardServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new CardServiceImpl(cardRepository, customerRepository);
    }

    @Test
    void create_shouldPersistCard_whenValid() {
        CardCreateRequest req = new CardCreateRequest();
        req.setCardNumber("4111111111111111");
        req.setCardType(CardType.CREDITO);
        req.setExpirationDate(LocalDate.of(2030, 12, 31));
        req.setCreditLimit(new BigDecimal("50000.00"));
        req.setCustomerId(10L);

        Customer customer = Customer.builder().id(10L).firstName("Juan").lastName("Pérez").build();

        when(cardRepository.existsByCardNumber("4111111111111111")).thenReturn(false);
        when(customerRepository.findById(10L)).thenReturn(Optional.of(customer));
        when(cardRepository.save(any(Card.class))).thenAnswer(inv -> {
            Card c = inv.getArgument(0);
            c.setId(1L);
            return c;
        });

        Card saved = service.create(req);

        assertThat(saved.getId()).isEqualTo(1L);
        assertThat(saved.getCardNumber()).isEqualTo("4111111111111111");
        assertThat(saved.getCardType()).isEqualTo(CardType.CREDITO);
        assertThat(saved.getExpirationDate()).isEqualTo(LocalDate.of(2030, 12, 31));
        assertThat(saved.getCreditLimit()).isEqualByComparingTo("50000.00");
        assertThat(saved.getCustomer().getId()).isEqualTo(10L);

        verify(cardRepository).existsByCardNumber("4111111111111111");
        verify(customerRepository).findById(10L);
        verify(cardRepository).save(argThat(c ->
                "4111111111111111".equals(c.getCardNumber()) &&
                        c.getCardType() == CardType.CREDITO &&
                        LocalDate.of(2030, 12, 31).equals(c.getExpirationDate()) &&
                        c.getCreditLimit().compareTo(new BigDecimal("50000.00")) == 0 &&
                        c.getCustomer() == customer
        ));
    }

    @Test
    void create_shouldThrowDuplicate_whenCardNumberAlreadyExists() {
        CardCreateRequest req = new CardCreateRequest();
        req.setCardNumber("5555444433332222");

        when(cardRepository.existsByCardNumber("5555444433332222")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> service.create(req));

        verify(cardRepository).existsByCardNumber("5555444433332222");
        verify(customerRepository, never()).findById(anyLong());
        verify(cardRepository, never()).save(any());
    }

    @Test
    void create_shouldThrowNotFound_whenCustomerMissing() {
        CardCreateRequest req = new CardCreateRequest();
        req.setCardNumber("4000000000000002");
        req.setCardType(CardType.DEBITO);
        req.setExpirationDate(LocalDate.of(2029, 6, 30));
        req.setCreditLimit(new BigDecimal("0"));
        req.setCustomerId(99L);

        when(cardRepository.existsByCardNumber("4000000000000002")).thenReturn(false);
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.create(req));

        verify(cardRepository).existsByCardNumber("4000000000000002");
        verify(customerRepository).findById(99L);
        verify(cardRepository, never()).save(any());
    }

    @Test
    void update_shouldChangeTypeExpirationAndLimit_whenExists() {
        Card existing = Card.builder()
                .id(3L)
                .cardNumber("4111111111111111")
                .cardType(CardType.CREDITO)
                .expirationDate(LocalDate.of(2028, 1, 31))
                .creditLimit(new BigDecimal("10000.00"))
                .build();

        CardUpdateRequest req = new CardUpdateRequest();
        req.setCardType(CardType.DEBITO);
        req.setExpirationDate(LocalDate.of(2031, 12, 31));
        req.setCreditLimit(new BigDecimal("0"));

        when(cardRepository.findById(3L)).thenReturn(Optional.of(existing));
        when(cardRepository.save(any(Card.class))).thenAnswer(inv -> inv.getArgument(0));

        Card updated = service.update(3L, req);

        assertThat(updated.getCardType()).isEqualTo(CardType.DEBITO);
        assertThat(updated.getExpirationDate()).isEqualTo(LocalDate.of(2031, 12, 31));
        assertThat(updated.getCreditLimit()).isEqualByComparingTo("0");

        verify(cardRepository).findById(3L);
        verify(cardRepository).save(same(existing));
    }

    @Test
    void update_shouldThrowNotFound_whenCardMissing() {
        CardUpdateRequest req = new CardUpdateRequest();
        when(cardRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.update(404L, req));

        verify(cardRepository).findById(404L);
        verify(cardRepository, never()).save(any());
    }

    @Test
    void delete_shouldRemove_whenExists() {
        when(cardRepository.existsById(5L)).thenReturn(true);

        service.delete(5L);

        verify(cardRepository).existsById(5L);
        verify(cardRepository).deleteById(5L);
    }

    @Test
    void delete_shouldThrowNotFound_whenMissing() {
        when(cardRepository.existsById(77L)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> service.delete(77L));

        verify(cardRepository).existsById(77L);
        verify(cardRepository, never()).deleteById(anyLong());
    }

    @Test
    void getById_shouldReturn_whenExists() {
        Card c = Card.builder().id(8L).cardNumber("4222222222222").cardType(CardType.CREDITO).build();
        when(cardRepository.findById(8L)).thenReturn(Optional.of(c));

        Card result = service.getById(8L);

        assertThat(result.getId()).isEqualTo(8L);
        assertThat(result.getCardNumber()).isEqualTo("4222222222222");
        assertThat(result.getCardType()).isEqualTo(CardType.CREDITO);

        verify(cardRepository).findById(8L);
    }

    @Test
    void getById_shouldThrowNotFound_whenMissing() {
        when(cardRepository.findById(123L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.getById(123L));

        verify(cardRepository).findById(123L);
    }

    @Test
    void getAll_shouldReturnListFromRepository() {
        when(cardRepository.findAll()).thenReturn(
                List.of(Card.builder().id(1L).cardType(CardType.CREDITO).build(),
                        Card.builder().id(2L).cardType(CardType.DEBITO).build())
        );

        List<Card> all = service.getAll();

        assertThat(all).hasSize(2);
        assertThat(all.get(0).getCardType()).isEqualTo(CardType.CREDITO);
        assertThat(all.get(1).getCardType()).isEqualTo(CardType.DEBITO);

        verify(cardRepository).findAll();
    }

    @Test
    void getByCustomerId_shouldReturnCardsForCustomer() {
        when(cardRepository.findByCustomer_Id(10L)).thenReturn(
                List.of(Card.builder().id(11L).cardType(CardType.DEBITO).build())
        );

        List<Card> list = service.getByCustomerId(10L);

        assertThat(list).hasSize(1);
        assertThat(list.get(0).getCardType()).isEqualTo(CardType.DEBITO);

        verify(cardRepository).findByCustomer_Id(10L);
    }
}
