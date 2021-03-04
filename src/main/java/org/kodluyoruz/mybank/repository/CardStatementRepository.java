package org.kodluyoruz.mybank.repository;

import org.kodluyoruz.mybank.entity.transaction.CardStatement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CardStatementRepository extends JpaRepository<CardStatement, Long> {

    List<CardStatement> findByCardNumber(String cardNumber);
}
