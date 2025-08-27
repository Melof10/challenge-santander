package com.melof10.santander.controller.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CustomerCreateRequest {

    @NotBlank()
    @Size(max = 100)
    private String firstName;

    @NotBlank @Size(max = 100)
    private String lastName;

    @NotBlank @Size(max = 20)
    private String document;

    @Email @Size(max = 100)
    private String email;

    @Size(max = 20)
    private String phone;
}

