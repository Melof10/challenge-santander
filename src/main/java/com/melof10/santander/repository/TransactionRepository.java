package com.melof10.santander.repository;

import com.melof10.santander.entity.Transaction;
import com.melof10.santander.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Page<Transaction> findBySourceAccount_IdOrDestinationAccount_IdOrderByDateDesc(
            Long sourceAccountId, Long destinationAccountId, Pageable pageable);

    List<Transaction> findTop10BySourceAccount_IdOrDestinationAccount_IdOrderByDateDesc(
            Long sourceAccountId, Long destinationAccountId);

    Page<Transaction> findByTypeAndSourceAccount_IdOrTypeAndDestinationAccount_IdOrderByDateDesc(
            TransactionType type1, Long accountAsSource, TransactionType type2, Long accountAsDestination, Pageable pageable);

    @Query("""
           SELECT t FROM Transaction t
           WHERE (t.sourceAccount.id = :accountId OR t.destinationAccount.id = :accountId)
             AND t.date BETWEEN :from AND :to
           ORDER BY t.date DESC
           """)
    Page<Transaction> findStatementBetweenDates(Long accountId, LocalDateTime from, LocalDateTime to, Pageable pageable);

    @Query("""
           SELECT COALESCE(SUM(t.amount), 0)
           FROM Transaction t
           WHERE t.type = :type
             AND (t.sourceAccount.id = :accountId OR t.destinationAccount.id = :accountId)
           """)
    BigDecimal sumAmountByTypeForAccount(TransactionType type, Long accountId);

    @Query("""
           select t
           from Transaction t
           left join fetch t.sourceAccount
           left join fetch t.destinationAccount
           """)
    List<Transaction> findAllWithAccounts();
}

