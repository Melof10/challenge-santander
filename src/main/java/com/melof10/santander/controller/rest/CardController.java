package com.melof10.santander.controller.rest;

import com.melof10.santander.controller.request.CardCreateRequest;
import com.melof10.santander.controller.request.CardUpdateRequest;
import com.melof10.santander.entity.Card;
import com.melof10.santander.service.ICardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class CardController {

    private final ICardService service;

    @PostMapping
    public ResponseEntity<Card> create(@Valid @RequestBody CardCreateRequest req) {
        Card saved = service.create(req);
        return ResponseEntity.created(URI.create("/api/cards/" + saved.getId())).body(saved);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Card> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<Card>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Card> update(@PathVariable Long id, @Valid @RequestBody CardUpdateRequest req) {
        return ResponseEntity.ok(service.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/by-customer/{customerId}")
    public ResponseEntity<List<Card>> getByCustomer(@PathVariable Long customerId) {
        return ResponseEntity.ok(service.getByCustomerId(customerId));
    }
}

