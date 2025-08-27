package com.melof10.santander.controller.request;

import com.melof10.santander.enums.TransactionType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionCreateRequest {

    @NotNull
    private TransactionType type;

    @NotNull @Positive
    private BigDecimal amount;

    private Long sourceAccountId;
    private Long destinationAccountId;
}

