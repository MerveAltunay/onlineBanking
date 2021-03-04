package org.kodluyoruz.mybank;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kodluyoruz.mybank.entity.Customer;
import org.kodluyoruz.mybank.entity.account.DemandDepositAccount;
import org.kodluyoruz.mybank.entity.account.SavingsAccount;
import org.kodluyoruz.mybank.helper.*;
import org.kodluyoruz.mybank.repository.CustomerRepository;
import org.kodluyoruz.mybank.service.CustomerService;
import org.kodluyoruz.mybank.service.DemandAccountService;
import org.kodluyoruz.mybank.service.SavingsAccountService;
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
public class TestAccount {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private DemandAccountService demandAccountService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private SavingsAccountService savingsAccountService;


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
    public void checkCreateAccount(){

        Customer customer = customerRepository.findByName("Merve");

        CreateAccountRequest request = new CreateAccountRequest();

        request.setCustomerId(customer.getCustomerId());
        request.setAccountType("US");

        ResponseEntity expected = ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body("Invalid account type");

        ResponseEntity actual = demandAccountService.createDemandAccount(request);

        Assertions.assertEquals(expected,actual);

    }

    @Test
    public void deleteAccountHaveMoney() throws IOException {

        Customer customer = customerRepository.findByName("Merve");

        CreateAccountRequest request = new CreateAccountRequest();

        request.setCustomerId(customer.getCustomerId());
        request.setAccountType("USD");
        demandAccountService.createDemandAccount(request);

        DemandDepositAccount demandDepositAccount = customer.getDemandDepositAccounts().get(0);

        AccountBalanceTransaction balanceTransaction = new AccountBalanceTransaction();

        balanceTransaction.setAmount(100);
        balanceTransaction.setAccountNumber(demandDepositAccount.getAccountNumber());

        demandAccountService.addBalance(balanceTransaction);

        ResponseEntity expected = ResponseEntity.status(HttpStatus.LOCKED).body("Deposit account have money.Please empty your account first.");

        ResponseEntity actual = demandAccountService.deleteById(demandDepositAccount.getId());

        Assertions.assertEquals(expected,actual);

    }

    @Test
    public void sendMoneyToDeposit() throws IOException {

        Customer customer = customerRepository.findByName("Merve");

        CreateAccountRequest request = new CreateAccountRequest();

        request.setCustomerId(customer.getCustomerId());
        request.setAccountType("USD");
        demandAccountService.createDemandAccount(request);

        DemandDepositAccount demandDepositAccount = customer.getDemandDepositAccounts().get(0);
        demandDepositAccount.setBalance(100);

        CreateAccountRequest request1 = new CreateAccountRequest();

        request1.setCustomerId(customer.getCustomerId());
        request1.setAccountType("EUR");
        demandAccountService.createDemandAccount(request1);

        DemandDepositAccount demandDepositAccount1 = customer.getDemandDepositAccounts().get(1);
        demandDepositAccount1.setBalance(200);

        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setAmount(200);
        transferRequest.setFromIbanNo(demandDepositAccount.getIbanNo());
        transferRequest.setToIbanNo(demandDepositAccount1.getIbanNo());

        ResponseEntity expected = ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Sender don't have money for transaction");

        ResponseEntity actual = demandAccountService.sendMoneyToDeposit(transferRequest);

        Assertions.assertEquals(expected, actual);

    }

    @Test
    public void savingsAccountAnotherAccount() throws IOException {

        Customer customer = customerRepository.findByName("Merve");

        CreateAccountRequest request = new CreateAccountRequest();
        request.setCustomerId(customer.getCustomerId());
        request.setAccountType("TRY");

        savingsAccountService.createSavingsAccount(request);

        SavingsAccount savingsAccount = customer.getSavingsAccounts().get(0);

        Customer customer1 = customerRepository.findByName("Sevilay");

        CreateAccountRequest request1 = new CreateAccountRequest();
        request1.setCustomerId(customer1.getCustomerId());
        request1.setAccountType("EUR");

        demandAccountService.createDemandAccount(request1);

        DemandDepositAccount demandDepositAccount = customer1.getDemandDepositAccounts().get(0);

        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setAmount(100);
        transferRequest.setFromIbanNo(savingsAccount.getIbanNo());
        transferRequest.setToIbanNo(demandDepositAccount.getIbanNo());

        ResponseEntity expected =  ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Savings account just send own account");

        ResponseEntity actual = savingsAccountService.sendMoneyToDeposit(transferRequest);

        Assertions.assertEquals(expected, actual);

    }

}
