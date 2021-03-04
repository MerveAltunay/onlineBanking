package org.kodluyoruz.mybank;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kodluyoruz.mybank.entity.Customer;
import org.kodluyoruz.mybank.entity.card.CreditCard;
import org.kodluyoruz.mybank.helper.*;
import org.kodluyoruz.mybank.repository.CustomerRepository;
import org.kodluyoruz.mybank.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class TestCustomer {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CreditCardService creditCardService;

    @TestConfiguration
    static class CustomerServiceTestConfig {

        @Bean
        public CustomerService customerService(){
            return new CustomerService();
        }

    }

    @TestConfiguration
    static class DemandAccountServiceTestConfig {

        @Bean
        public DemandAccountService demandAccountService(){
            return new DemandAccountService();
        }

    }

    @TestConfiguration
    static class CreditCardServiceTestConfig {

        @Bean
        public CreditCardService creditCardService(){
            return new CreditCardService();
        }

    }

    @TestConfiguration
    static class SavingsAccountServiceTestConfig {

        @Bean
        public SavingsAccountService savingsAccountService(){
            return new SavingsAccountService();
        }

    }

    @BeforeEach
    public void createCustomer(){

        CreateCustomerRequest request = new CreateCustomerRequest();

        CreateAddressRequest address = new CreateAddressRequest("Ankara","Melike","8","06010");

        request.setAddress(address);
        request.setName("Merve");
        request.setEmail("merve@gmail.com");
        request.setSurname("Altunay");
        request.setCitizenshipNumber("12346789121");

        customerService.createCustomer(request);

        CreateCustomerRequest request1 = new CreateCustomerRequest();
        CreateAddressRequest address1 = new CreateAddressRequest("Istanbul","Yigitler","15","34010");

        request1.setAddress(address1);
        request1.setName("Sevilay");
        request1.setEmail("sevilay@gmail.com");
        request1.setSurname("Altun");
        request1.setCitizenshipNumber("98765432132");

        customerService.createCustomer(request1);

    }

    @Test
    public void checkCustomer(){

        Customer customer = customerRepository.findByName("Merve");
        String name = customer.getName();

        Assertions.assertEquals("Merve",name);
    }

    @Test
    public void checkUpdateCustomerMail(){

        Customer customer = customerRepository.findByName("Merve");

        CreateCustomerRequest request = new CreateCustomerRequest();

        request.setCitizenshipNumber(customer.getCitizenshipNumber());
        request.setEmail(customer.getEmail());

        UpdateEmail email = new UpdateEmail();
        email.setEmail("altunaymerve@gmail.com");

        ResponseEntity expected = ResponseEntity.status(HttpStatus.OK).body("Customer email is updated");

        ResponseEntity actual = customerService.updateCustomerMail(email, customer.getCitizenshipNumber());

        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void checkUpdateCustomerAddress(){

        Customer customer = customerRepository.findByName("Sevilay");

        CreateCustomerRequest request = new CreateCustomerRequest();
        CreateAddressRequest address = new CreateAddressRequest();

        request.setCitizenshipNumber(customer.getCitizenshipNumber());
        request.setAddress(address);

        address.setStreet("Emrah");
        address.setCity("Ankara");
        address.setHouseNumber("9");
        address.setZipCode("06010");

        ResponseEntity expected = ResponseEntity.status(HttpStatus.OK).body("Customer address is updated");

        ResponseEntity actual = customerService.updateCustomerAddress(address, customer.getCitizenshipNumber());

        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void deleteCustomerWithDebt() throws Exception {

        Customer customer = customerRepository.findByName("Merve");

        CreditCardRequest request = new CreditCardRequest();
        request.setCustomerId(customer.getCustomerId());
        request.setCardLimit(1000);

        creditCardService.createCreditCard(request);

        CreditCard creditCard = customer.getCreditCards().get(0);

        BalanceTransactionRequest balanceTransaction = new BalanceTransactionRequest();
        balanceTransaction.setCardNumber(creditCard.getCardNumber());
        balanceTransaction.setAmount(800);

        creditCardService.withDrawCreditCard(balanceTransaction);

        customerService.deleteCustomer(customer.getCitizenshipNumber());

        Assertions.assertNotNull(customer);

    }


}
