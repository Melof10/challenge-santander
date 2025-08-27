package com.melof10.santander.service.impl;

import com.melof10.santander.controller.request.CardCreateRequest;
import com.melof10.santander.controller.request.CardUpdateRequest;
import com.melof10.santander.entity.Card;
import com.melof10.santander.entity.Customer;
import com.melof10.santander.exception.DuplicateResourceException;
import com.melof10.santander.exception.NotFoundException;
import com.melof10.santander.repository.CardRepository;
import com.melof10.santander.repository.CustomerRepository;
import com.melof10.santander.service.ICardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CardServiceImpl implements ICardService {

    private final CardRepository cardRepository;
    private final CustomerRepository customerRepository;

    @Override
    public Card create(CardCreateRequest req) {
        if (cardRepository.existsByCardNumber(req.getCardNumber())) {
            throw new DuplicateResourceException("La tarjeta ya existe: " + req.getCardNumber());
        }
        Customer customer = customerRepository.findById(req.getCustomerId())
                .orElseThrow(() -> new NotFoundException("Cliente no encontrado: " + req.getCustomerId()));

        Card card = Card.builder()
                .cardNumber(req.getCardNumber())
                .cardType(req.getCardType())
                .expirationDate(req.getExpirationDate())
                .creditLimit(req.getCreditLimit())
                .customer(customer)
                .build();
        return cardRepository.save(card);
    }

    @Override
    public Card update(Long id, CardUpdateRequest req) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Tarjeta no encontrada: " + id));
        card.setCardType(req.getCardType());
        card.setExpirationDate(req.getExpirationDate());
        card.setCreditLimit(req.getCreditLimit());
        return cardRepository.save(card);
    }

    @Override
    public void delete(Long id) {
        if (!cardRepository.existsById(id)) {
            throw new NotFoundException("Tarjeta no encontrada: " + id);
        }
        cardRepository.deleteById(id);
    }

    @Override @Transactional(readOnly = true)
    public Card getById(Long id) {
        return cardRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Tarjeta no encontrada: " + id));
    }

    @Override @Transactional(readOnly = true)
    public List<Card> getAll() {
        return cardRepository.findAll();
    }

    @Override @Transactional(readOnly = true)
    public List<Card> getByCustomerId(Long customerId) {
        return cardRepository.findByCustomer_Id(customerId);
    }
}

