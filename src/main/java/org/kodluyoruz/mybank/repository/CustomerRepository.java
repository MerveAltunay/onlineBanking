package org.kodluyoruz.mybank.repository;

import org.kodluyoruz.mybank.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends JpaRepository<Customer,Long> {

    Customer findByCitizenshipNumber(String citizenshipNumber);

    Customer findByCustomerId(long customerId);

    Customer findByName(String name);

}
