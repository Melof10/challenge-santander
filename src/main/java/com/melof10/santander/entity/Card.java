package com.melof10.santander.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.melof10.santander.enums.CardType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(
        name = "tarjeta",
        indexes = {
                @Index(name = "idx_tarjeta_numero", columnList = "cardNumber", unique = true),
                @Index(name = "idx_tarjeta_cliente", columnList = "id_cliente")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, unique = true, length = 19, updatable = false)
    private String cardNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CardType cardType;

    @Column(nullable = false)
    private LocalDate expirationDate;

    @Column(precision = 18, scale = 2)
    private BigDecimal creditLimit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente", nullable = false)
    @JsonBackReference
    private Customer customer;
}
