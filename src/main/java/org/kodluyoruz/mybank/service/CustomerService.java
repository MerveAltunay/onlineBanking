package org.kodluyoruz.mybank.service;

import org.kodluyoruz.mybank.entity.Address;
import org.kodluyoruz.mybank.entity.Customer;
import org.kodluyoruz.mybank.entity.account.DemandDepositAccount;
import org.kodluyoruz.mybank.entity.account.SavingsAccount;
import org.kodluyoruz.mybank.entity.card.CreditCard;
import org.kodluyoruz.mybank.repository.CustomerRepository;
import org.kodluyoruz.mybank.repository.DemandAccountRepository;
import org.kodluyoruz.mybank.repository.SavingsAccountRepository;
import org.kodluyoruz.mybank.helper.CreateAddressRequest;
import org.kodluyoruz.mybank.helper.CreateCustomerRequest;
import org.kodluyoruz.mybank.helper.UpdateEmail;
import org.kodluyoruz.mybank.helper.UpdatePhoneNumber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private DemandAccountRepository demandAccountRepository;

    @Autowired
    private SavingsAccountRepository savingsAccountRepository;

    public CustomerService(CustomerRepository customerRepository, DemandAccountRepository demandAccountRepository, SavingsAccountRepository savingsAccountRepository) {

        this.customerRepository = customerRepository;
        this.demandAccountRepository = demandAccountRepository;
        this.savingsAccountRepository = savingsAccountRepository;
    }

    public CustomerService() {

    }

    public ResponseEntity<Object> createCustomer(CreateCustomerRequest customerRequest){

        Customer customer = customerRepository.findByCitizenshipNumber(customerRequest.getCitizenshipNumber());

        if(customer != null){
            return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body("There is a customer with this number.You can't register again");
        }
        if(customerRequest.getCitizenshipNumber().length() != 11){
            return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body("Citizenship number must be 11 in length");
        }

        customer = new Customer();
        Address address = new Address();

        address.setCity(customerRequest.getAddress().getCity());
        address.setStreet(customerRequest.getAddress().getStreet());
        address.setHouseNumber(customerRequest.getAddress().getHouseNumber());
        address.setZipCode(customerRequest.getAddress().getZipCode());

        customer.setEmail(customerRequest.getEmail());
        customer.setName(customerRequest.getName());
        customer.setSurname(customerRequest.getSurname());
        customer.setPhoneNumber(customerRequest.getPhoneNumber());
        customer.setCitizenshipNumber(customerRequest.getCitizenshipNumber());
        customer.setAddress(address);

        customerRepository.save(customer);
        return ResponseEntity.status(HttpStatus.OK).body("Customer is created");
    }

    public List<Customer> getAllCustomer() {

        return customerRepository.findAll();
    }

    public Customer findByCitizenshipNumber(String citizenshipNumber) {

        return customerRepository.findByCitizenshipNumber(citizenshipNumber);
    }

    public ResponseEntity<Object> updateCustomerAddress(CreateAddressRequest request, String citizenshipNumber){

        Customer customer = customerRepository.findByCitizenshipNumber(citizenshipNumber);

        if(customer == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Customer is not found");
        }

        Address address = customer.getAddress();

        address.setCity(request.getCity());
        address.setStreet(request.getStreet());
        address.setHouseNumber(request.getHouseNumber());
        address.setZipCode(request.getZipCode());

        customer.setAddress(address);
        customerRepository.save(customer);
        return ResponseEntity.status(HttpStatus.OK).body("Customer address is updated");

    }

    public ResponseEntity<Object> updateCustomerPhone(UpdatePhoneNumber phoneNumber, String citizenshipNumber){

        Customer customer = customerRepository.findByCitizenshipNumber(citizenshipNumber);

        if(customer == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Customer is not found");
        }

        customer.setPhoneNumber(phoneNumber.getPhoneNumber());
        customerRepository.save(customer);
        return ResponseEntity.status(HttpStatus.OK).body("Customer phone number is updated");

    }
    public ResponseEntity<Object> updateCustomerMail(UpdateEmail email, String citizenshipNumber){

        Customer customer = customerRepository.findByCitizenshipNumber(citizenshipNumber);

        if(customer == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Customer is not found");
        }

        customer.setEmail(email.getEmail());
        customerRepository.save(customer);
        return ResponseEntity.status(HttpStatus.OK).body("Customer email is updated");

    }

    public ResponseEntity<Object> deleteCustomer(String citizenshipNumber) {

        Customer customer = customerRepository.findByCitizenshipNumber(citizenshipNumber);

        if(customer != null){

            List<DemandDepositAccount> deposits = customer.getDemandDepositAccounts();
            List<SavingsAccount> savings = customer.getSavingsAccounts();

            for(int i=0; i<deposits.size(); i++)
            {
                if(deposits.get(i).getBalance() != 0){
                    return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("You have balance in your account.Not deleted customer");
                }
            }

            for(int i=0; i<savings.size(); i++)
            {
                if(savings.get(i).getBalance() != 0){
                    return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("You have balance in your account.Not deleted customer");
                }
            }

            List<CreditCard> creditCards = customer.getCreditCards();

            for(int i=0; i<creditCards.size(); i++)
            {
                if(creditCards.get(i).getCardLimit() - creditCards.get(i).getRemainingCreditLimit() != 0){
                    return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("You have credit debt.Not deleted customer");
                }
            }

            customerRepository.delete(customer);
            return ResponseEntity.status(HttpStatus.OK).body("Customer is deleted");

        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Customer is not found");
    }
}
