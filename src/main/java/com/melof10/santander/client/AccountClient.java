package com.melof10.santander.client;

import com.melof10.santander.entity.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AccountClient {

    private final WebClient webClient;

    @Value("${santander.api.base-url}")
    private String baseUrl;

    public Optional<Account> getAccountById(Long id) {
        try {
            return webClient
                    .get()
                    .uri(baseUrl + "/{id}", id)
                    .retrieve()
                    .bodyToMono(Account.class)
                    .blockOptional();
        } catch (WebClientResponseException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return Optional.empty();
            }
            throw e;
        }
    }
}
