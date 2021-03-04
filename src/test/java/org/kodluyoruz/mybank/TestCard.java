package org.kodluyoruz.mybank;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kodluyoruz.mybank.entity.Customer;
import org.kodluyoruz.mybank.entity.account.DemandDepositAccount;
import org.kodluyoruz.mybank.entity.card.CreditCard;
import org.kodluyoruz.mybank.entity.card.DebitCard;
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
public class TestCard {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private DemandAccountService demandAccountService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private DebitCardService debitCardService;

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
    static class SavingsAccountServiceTestConfig {

        @Bean
        public SavingsAccountService savingsAccountService(){
            return new SavingsAccountService();
        }

    }

    @TestConfiguration
    static class DebitCardServiceTestConfig {

        @Bean
        public DebitCardService debitCardService(){
            return new DebitCardService();
        }

    }

    @TestConfiguration
    static class CreditCardServiceTestConfig {

        @Bean
        public CreditCardService creditCardService(){
            return new CreditCardService();
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

    }

    @Test
    public void debitCardWithDrawBalance() throws IOException {

        Customer customer = customerRepository.findByName("Merve");

        CreateAccountRequest request = new CreateAccountRequest();

        request.setCustomerId(customer.getCustomerId());
        request.setAccountType("USD");
        demandAccountService.createDemandAccount(request);

        DemandDepositAccount demandDepositAccount = customer.getDemandDepositAccounts().get(0);

        DebitCardRequest debitCardRequest = new DebitCardRequest();

        debitCardRequest.setAccountNumber(demandDepositAccount.getAccountNumber());
        debitCardService.createDebitCard(debitCardRequest);

        DebitCard debitCard = demandDepositAccount.getDebitCards().get(0);
        debitCard.getDemandDepositAccount().setBalance(50);

        BalanceTransactionRequest balanceTransactionRequest = new BalanceTransactionRequest();

        balanceTransactionRequest.setAmount(100);
        balanceTransactionRequest.setCardNumber(debitCard.getCardNumber());

        ResponseEntity expected = ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("Your account balance is not enough to withdraw this money");

        ResponseEntity actual = debitCardService.withDrawBalance(balanceTransactionRequest);

        Assertions.assertEquals(expected,actual);

    }

    @Test
    public void debtOnAccount() throws Exception {

        Customer customer = customerRepository.findByName("Merve");

        CreateAccountRequest request = new CreateAccountRequest();

        request.setCustomerId(customer.getCustomerId());
        request.setAccountType("TRY");
        demandAccountService.createDemandAccount(request);

        DemandDepositAccount demandDepositAccount = customer.getDemandDepositAccounts().get(0);
        demandDepositAccount.setBalance(1500);

        CreditCardRequest creditCardRequest = new CreditCardRequest();

        creditCardRequest.setCardLimit(1000);
        creditCardRequest.setCustomerId(customer.getCustomerId());
        creditCardService.createCreditCard(creditCardRequest);

        CreditCard creditCard = customer.getCreditCards().get(0);

        BalanceTransactionRequest balanceTransaction = new BalanceTransactionRequest();
        balanceTransaction.setCardNumber(creditCard.getCardNumber());
        balanceTransaction.setAmount(800);

        creditCardService.withDrawCreditCard(balanceTransaction);

        DebtOnAccount debtOnAccount = new DebtOnAccount();

        debtOnAccount.setAccountNumber(demandDepositAccount.getAccountNumber());
        debtOnAccount.setAmount(balanceTransaction.getAmount());
        debtOnAccount.setCreditCardNumber(creditCard.getCardNumber());

        ResponseEntity expected = ResponseEntity.status(HttpStatus.OK).body("Credit card debt amounting to " + balanceTransaction.getAmount() + " TL has been paid");


        ResponseEntity actual = creditCardService.debtOnAccount(debtOnAccount);

        Assertions.assertEquals(expected,actual);


    }

}
