package org.kodluyoruz.mybank.controller;

import org.kodluyoruz.mybank.entity.Address;
import org.kodluyoruz.mybank.repository.AddressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/customer/address")
public class AddressController {

    @Autowired
    private AddressRepository addressRepository;

    @GetMapping("/findAll")
    public List<Address> findAll(){

        return addressRepository.findAll();
    }
}
