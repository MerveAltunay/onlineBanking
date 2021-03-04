package org.kodluyoruz.mybank.repository;

import org.kodluyoruz.mybank.entity.Address;
import org.kodluyoruz.mybank.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

    List<Customer> findByCity(String city);
}
