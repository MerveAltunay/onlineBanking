package org.kodluyoruz.mybank.controller;

import org.kodluyoruz.mybank.entity.Customer;
import org.kodluyoruz.mybank.helper.CreateAddressRequest;
import org.kodluyoruz.mybank.helper.CreateCustomerRequest;
import org.kodluyoruz.mybank.helper.UpdateEmail;
import org.kodluyoruz.mybank.helper.UpdatePhoneNumber;
import org.kodluyoruz.mybank.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/customer")
public class CustomerController{

    @Autowired
    CustomerService customerService;

    @PostMapping(value = "/create")
    public ResponseEntity<Object> createCustomer(@RequestBody CreateCustomerRequest customerRequest) {

        return customerService.createCustomer(customerRequest);
    }

    @GetMapping("/all")
    public List<Customer> getAllCustomers() {

        return customerService.getAllCustomer();
    }

    @GetMapping("/{citizenshipNumber}")
    public Customer getCustomer(@PathVariable String citizenshipNumber) {

        return customerService.findByCitizenshipNumber(citizenshipNumber);
    }

    @PutMapping("update/mail/{citizenshipNumber}")
    public ResponseEntity<Object> updateCustomerMail(@RequestBody UpdateEmail email, @PathVariable String citizenshipNumber){

        return customerService.updateCustomerMail(email,citizenshipNumber);

    }

    @PutMapping("update/address/{citizenshipNumber}")
    public ResponseEntity<Object> updateAddress(@RequestBody CreateAddressRequest addressRequest, @PathVariable String citizenshipNumber){

        return customerService.updateCustomerAddress(addressRequest,citizenshipNumber);

    }

    @PutMapping("update/phoneNumber/{citizenshipNumber}")
    public ResponseEntity<Object> updatePhone(@RequestBody UpdatePhoneNumber number, @PathVariable String citizenshipNumber){

        return customerService.updateCustomerPhone(number,citizenshipNumber);

    }

    @DeleteMapping("/delete/{citizenshipNumber}")
    public ResponseEntity<Object> deleteCustomer(@PathVariable String citizenshipNumber) {

        return customerService.deleteCustomer(citizenshipNumber);
    }

}
