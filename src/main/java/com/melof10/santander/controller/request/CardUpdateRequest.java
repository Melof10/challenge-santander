package com.melof10.santander.controller.request;

import com.melof10.santander.enums.CardType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CardUpdateRequest {

    @NotNull
    private CardType cardType;

    @NotNull
    private LocalDate expirationDate;

    @PositiveOrZero
    private BigDecimal creditLimit;
}

