package org.kodluyoruz.mybank.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.kodluyoruz.mybank.entity.Customer;
import org.kodluyoruz.mybank.entity.account.DemandDepositAccount;
import org.kodluyoruz.mybank.entity.account.SavingsAccount;
import org.kodluyoruz.mybank.entity.card.CreditCard;
import org.kodluyoruz.mybank.entity.card.DebitCard;
import org.kodluyoruz.mybank.entity.transaction.CardStatement;
import org.kodluyoruz.mybank.entity.transaction.Transaction;
import org.kodluyoruz.mybank.domain.CardNumberGenerator;
import org.kodluyoruz.mybank.domain.CurrencyClass;
import org.kodluyoruz.mybank.repository.*;
import org.kodluyoruz.mybank.helper.BalanceTransactionRequest;
import org.kodluyoruz.mybank.helper.CreditCardRequest;
import org.kodluyoruz.mybank.helper.DebtOnAccount;
import org.kodluyoruz.mybank.helper.DebtOnDebitRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class CreditCardService {

    @Autowired
    private CreditCardRepository creditCardRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private DemandAccountRepository demandAccountRepository;

    @Autowired
    private SavingsAccountRepository savingsAccountRepository;

    @Autowired
    private DebitCardRepository debitCardRepository;

    @Autowired
    private CardStatementRepository cardStatementRepository;



    public CreditCardService(CreditCardRepository creditCardRepository, CustomerRepository customerRepository, TransactionRepository transactionRepository, DemandAccountRepository demandAccountRepository, SavingsAccountRepository savingsAccountRepository, DebitCardRepository debitCardRepository, CardStatementRepository cardStatementRepository) {
        this.creditCardRepository = creditCardRepository;
        this.customerRepository = customerRepository;
        this.transactionRepository = transactionRepository;
        this.demandAccountRepository = demandAccountRepository;
        this.savingsAccountRepository = savingsAccountRepository;
        this.debitCardRepository = debitCardRepository;
        this.cardStatementRepository = cardStatementRepository;
    }

    public CreditCardService() {

    }

    public ResponseEntity<Object> createCreditCard(CreditCardRequest creditCardRequest){

        CreditCard creditCard = new CreditCard();

        Customer customer = customerRepository.findByCustomerId(creditCardRequest.getCustomerId());

        if(customer != null){

            boolean isUnique = true;

            while (isUnique){

                CardNumberGenerator cardNumberGenerator = new CardNumberGenerator();
                String cardNumber = cardNumberGenerator.createCardNumber();

                if(creditCardRepository.findByCardNumber(cardNumber) != null){
                    continue;
                }

                creditCard.setCardNumber(cardNumber);

                isUnique = false;

            }

            creditCard.setCustomerId(customer.getCustomerId());
            creditCard.setCustomer(customer);
            creditCard.setCardLimit(creditCardRequest.getCardLimit());
            creditCard.setRemainingCreditLimit(creditCardRequest.getCardLimit());
            customer.getCreditCards().add(creditCard);
            customerRepository.save(customer);
            return ResponseEntity.status(HttpStatus.CREATED).body("Credit card is created");

        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Customer is not found");
    }


    public List<CreditCard> findAll() {

        return creditCardRepository.findAll();
    }

    public ResponseEntity<Object> withDrawCreditCard(BalanceTransactionRequest balanceTransactionRequest) throws Exception {

        CreditCard creditCard = creditCardRepository.findByCardNumber(balanceTransactionRequest.getCardNumber());

        if(creditCard != null){

            double amount = balanceTransactionRequest.getAmount();

            if(amount < creditCard.getRemainingCreditLimit()){

                creditCard.setRemainingCreditLimit(creditCard.getRemainingCreditLimit() - amount);

                    CardStatement cardStatement = new CardStatement(0L,balanceTransactionRequest.getCardNumber(),new Timestamp(System.currentTimeMillis()),"Withdraw Balance","CreditCard");Thread.sleep(5000);
                    cardStatementRepository.save(cardStatement);
                    transactionRepository.save(new Transaction(0L, balanceTransactionRequest.getCardNumber(),balanceTransactionRequest.getCardNumber(), amount, new Timestamp(System.currentTimeMillis()),"Credit Card -> Withdraw Balance"));
                    creditCardRepository.save(creditCard);

                    return ResponseEntity.status(HttpStatus.OK).body("Credit card balance has been reduced");
            }

            else
                return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("Credit card balance is not enough for this transaction");
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Credit card is not found");
    }

    public ResponseEntity<Object> debtInquiry(String creditCardNumber) throws IOException {

        CreditCard creditCard = creditCardRepository.findByCardNumber(creditCardNumber);

        if(creditCard != null){

            double debt = creditCard.getCardLimit() - creditCard.getRemainingCreditLimit();
            return ResponseEntity.status(HttpStatus.OK).body("Credit card debt is:" + debt);
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Credit card is not found");
    }

    public ResponseEntity<Object> debtOnAccount(DebtOnAccount debtOnAccount) throws IOException{

        CreditCard creditCard = creditCardRepository.findByCardNumber(debtOnAccount.getCreditCardNumber());

        ObjectMapper objectMapper = new ObjectMapper();
        URL url = new URL("https://api.exchangeratesapi.io/latest?base=TRY");
        CurrencyClass currencyClass =objectMapper.readValue(url,CurrencyClass.class);

        if(creditCard != null){

            Customer customer = customerRepository.findByCustomerId(creditCard.getCustomerId());

            DemandDepositAccount demandDepositAccount = demandAccountRepository.findByAccountNumber(debtOnAccount.getAccountNumber());
            SavingsAccount savingsAccount = savingsAccountRepository.findByAccountNumber(debtOnAccount.getAccountNumber());

            if(demandDepositAccount != null){

                if(demandDepositAccount.getCustomerId() == creditCard.getCustomerId()){

                    double payment = debtOnAccount.getAmount();
                    double currencyAsset = demandDepositAccount.getBalance() / currencyClass.getRates().get(demandDepositAccount.getAccountType());

                    if(payment < currencyAsset){

                        creditCard.setRemainingCreditLimit(creditCard.getRemainingCreditLimit() + payment);
                        demandDepositAccount.setBalance(demandDepositAccount.getBalance() - (payment * currencyClass.getRates().get(demandDepositAccount.getAccountType())));

                        CardStatement cardStatement = new CardStatement(0L,debtOnAccount.getCreditCardNumber(),new Timestamp(System.currentTimeMillis()),"Debt Payment","CreditCard");
                        cardStatementRepository.save(cardStatement);

                        creditCardRepository.save(creditCard);
                        demandAccountRepository.save(demandDepositAccount);
                        customerRepository.save(customer);

                        transactionRepository.save(new Transaction(0L, debtOnAccount.getAccountNumber(),debtOnAccount.getCreditCardNumber(), debtOnAccount.getAmount(), new Timestamp(System.currentTimeMillis()),"Deposit -> Credit Card"));
                        return ResponseEntity.status(HttpStatus.OK).body("Credit card debt amounting to " + payment + " TL has been paid");

                    }
                    else {
                        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("Your balance is not enough to pay " + payment + " credit card debt");
                    }
                }
                else {
                    return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("Account and credit card not match");
                }
            }
            if(savingsAccount != null){

                if(savingsAccount.getCustomerId() == creditCard.getCustomerId()){

                    double payment = debtOnAccount.getAmount();
                    double currencyAsset = savingsAccount.getBalance() / currencyClass.getRates().get(savingsAccount.getAccountType());

                    if(payment < currencyAsset){

                        creditCard.setRemainingCreditLimit(creditCard.getRemainingCreditLimit() + payment);
                        savingsAccount.setBalance(savingsAccount.getBalance() - (payment * currencyClass.getRates().get(savingsAccount.getAccountType())));

                        CardStatement cardStatement = new CardStatement(0L,debtOnAccount.getCreditCardNumber(),new Timestamp(System.currentTimeMillis()),"Debt Payment","CreditCard");
                        cardStatementRepository.save(cardStatement);

                        creditCardRepository.save(creditCard);
                        savingsAccountRepository.save(savingsAccount);
                        customerRepository.save(customer);

                        transactionRepository.save(new Transaction(0L, debtOnAccount.getAccountNumber(),debtOnAccount.getCreditCardNumber(), debtOnAccount.getAmount(), new Timestamp(System.currentTimeMillis()),"Savings -> Credit Card"));
                        return ResponseEntity.status(HttpStatus.OK).body("Credit card debt amounting to " + payment + " TL has been paid");

                    }
                    else{
                        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("Your balance is not enough to pay " + payment + " credit card debt");
                    }
                }
                else{
                    return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("Account and credit card are not match");
                }
            }
            else{
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Account is not found");
            }
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Credit card is not found");
    }

    public ResponseEntity<Object> debtOnDebitCard(DebtOnDebitRequest request) throws IOException {

        DebitCard debitCard = debitCardRepository.findByCardNumber(request.getDebitCardNumber());
        CreditCard creditCard = creditCardRepository.findByCardNumber(request.getCreditCardNumber());

        if(debitCard == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Debit card is not found");
        }

        if(creditCard == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Credit card is not found");
        }

        DemandDepositAccount demandDepositAccount = debitCard.getDemandDepositAccount();

        ObjectMapper objectMapper = new ObjectMapper();
        URL url = new URL("https://api.exchangeratesapi.io/latest?base=TRY");
        CurrencyClass currencyClass = objectMapper.readValue(url,CurrencyClass.class);

        String accountType = demandDepositAccount.getAccountType();

        double accountTRYMoney =  demandDepositAccount.getBalance() / currencyClass.getRates().get(accountType);

        if(accountTRYMoney >= request.getAmount()){

            creditCard.setRemainingCreditLimit(creditCard.getRemainingCreditLimit() + request.getAmount());

            double debtPaidOnAccountMoneyType = currencyClass.getRates().get(accountType) * request.getAmount();
            demandDepositAccount.setBalance(demandDepositAccount.getBalance() - debtPaidOnAccountMoneyType);

            CardStatement cardStatement = new CardStatement(0L,request.getCreditCardNumber(),new Timestamp(System.currentTimeMillis()),"Debt Payment","CreditCard");
            cardStatementRepository.save(cardStatement);

            demandAccountRepository.save(demandDepositAccount);
            creditCardRepository.save(creditCard);

            return ResponseEntity.status(HttpStatus.OK).body("Debt payment transaction is completed");

        }

        return ResponseEntity.status(HttpStatus.OK).body("Insufficient balance for debt payment");
    }

    public ResponseEntity<Object> deleteCreditCard(String cardNumber){

        CreditCard creditCard = creditCardRepository.findByCardNumber(cardNumber);

        if(cardNumber == null){

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Credit card is not found");
        }

        double debt = creditCard.getCardLimit() - creditCard.getRemainingCreditLimit();

        if(debt != 0){

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Credit card have a debt");
        }

        creditCardRepository.delete(creditCard);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Credit card is deleted");

    }
}
