package org.kodluyoruz.mybank.repository;

import org.kodluyoruz.mybank.entity.card.DebitCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DebitCardRepository extends JpaRepository<DebitCard, Long> {

    DebitCard findByCardNumber(String cardNumber);

}
