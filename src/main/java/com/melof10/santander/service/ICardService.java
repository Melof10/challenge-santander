package com.melof10.santander.service;

import com.melof10.santander.controller.request.CardCreateRequest;
import com.melof10.santander.controller.request.CardUpdateRequest;
import com.melof10.santander.entity.Card;

import java.util.List;

public interface ICardService {

    Card create(CardCreateRequest req);
    Card update(Long id, CardUpdateRequest req);
    void delete(Long id);
    Card getById(Long id);
    List<Card> getAll();
    List<Card> getByCustomerId(Long customerId);
}

