package com.melof10.santander.entity;

import com.melof10.santander.enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "transaccion",
        indexes = {
                @Index(name = "idx_tx_cuenta_origen", columnList = "id_cuenta_origen"),
                @Index(name = "idx_tx_cuenta_destino", columnList = "id_cuenta_destino"),
                @Index(name = "idx_tx_fecha", columnList = "date")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false)
    private LocalDateTime date;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cuenta_origen")
    private Account sourceAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cuenta_destino")
    private Account destinationAccount;

    @PrePersist
    public void initDefaults() {
        if (date == null) date = LocalDateTime.now();
    }
}
