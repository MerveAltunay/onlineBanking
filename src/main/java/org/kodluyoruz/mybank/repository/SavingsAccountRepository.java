package org.kodluyoruz.mybank.repository;

import org.kodluyoruz.mybank.entity.account.SavingsAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SavingsAccountRepository extends JpaRepository<SavingsAccount, Long> {

    SavingsAccount findByAccountNumber(String accountNumber);

    SavingsAccount findByIbanNo(String toIbanNo);

}
