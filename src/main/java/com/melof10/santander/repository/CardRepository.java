package com.melof10.santander.repository;

import com.melof10.santander.entity.Card;
import com.melof10.santander.enums.CardType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CardRepository extends JpaRepository<Card, Long> {

    Optional<Card> findByCardNumber(String cardNumber);

    boolean existsByCardNumber(String cardNumber);

    List<Card> findByCustomer_Id(Long customerId);

    List<Card> findByCustomer_IdAndCardType(Long customerId, CardType cardType);
}

