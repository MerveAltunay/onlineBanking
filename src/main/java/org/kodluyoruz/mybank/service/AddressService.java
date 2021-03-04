package org.kodluyoruz.mybank.service;

import org.kodluyoruz.mybank.entity.Address;
import org.kodluyoruz.mybank.repository.AddressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AddressService {

    @Autowired
    private AddressRepository addressRepository;

    public AddressService(AddressRepository addressRepository) {

        this.addressRepository = addressRepository;
    }

    public List<Address> findAll(){

        return addressRepository.findAll();
    }
}
