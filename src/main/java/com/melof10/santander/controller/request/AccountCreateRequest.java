package com.melof10.santander.controller.request;

import com.melof10.santander.enums.AccountType;
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
public class AccountCreateRequest {

    @NotBlank
    @Size(max = 22)
    private String accountNumber;

    @NotNull
    private AccountType accountType;

    @NotNull @PositiveOrZero
    private BigDecimal initialBalance;

    @NotNull
    private Long customerId;
}

