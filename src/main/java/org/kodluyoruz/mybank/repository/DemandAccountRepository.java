package org.kodluyoruz.mybank.repository;

import org.kodluyoruz.mybank.entity.account.DemandDepositAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DemandAccountRepository extends JpaRepository<DemandDepositAccount,Long> {

    DemandDepositAccount findByAccountNumber(String accountNumber);

    DemandDepositAccount findByIbanNo(String ibanNo);

}
