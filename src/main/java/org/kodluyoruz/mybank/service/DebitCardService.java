package org.kodluyoruz.mybank.service;

import org.kodluyoruz.mybank.entity.Customer;
import org.kodluyoruz.mybank.entity.account.DemandDepositAccount;
import org.kodluyoruz.mybank.entity.card.DebitCard;
import org.kodluyoruz.mybank.entity.transaction.CardStatement;
import org.kodluyoruz.mybank.entity.transaction.Transaction;
import org.kodluyoruz.mybank.domain.CardNumberGenerator;
import org.kodluyoruz.mybank.repository.*;
import org.kodluyoruz.mybank.helper.BalanceTransactionRequest;
import org.kodluyoruz.mybank.helper.DebitCardRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;

@Service
public class DebitCardService {

    @Autowired
    private DemandAccountRepository demandAccountRepository;

    @Autowired
    private DebitCardRepository debitCardRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private CardStatementRepository cardStatementRepository;

    public DebitCardService(DemandAccountRepository demandAccountRepository, DebitCardRepository debitCardRepository, CustomerRepository customerRepository, TransactionRepository transactionRepository, CardStatementRepository cardStatementRepository) {
        this.demandAccountRepository = demandAccountRepository;
        this.debitCardRepository = debitCardRepository;
        this.customerRepository = customerRepository;
        this.transactionRepository = transactionRepository;
        this.cardStatementRepository = cardStatementRepository;
    }

    public DebitCardService() {

    }

    public ResponseEntity<Object> createDebitCard(DebitCardRequest debitCardRequest) {

        DebitCard debitCard = new DebitCard();
        DemandDepositAccount demandDepositAccount = demandAccountRepository.findByAccountNumber(debitCardRequest.getAccountNumber());

        if (demandDepositAccount != null) {

            boolean isUnique = true;

            while (isUnique) {

                CardNumberGenerator cardNumberGenerator = new CardNumberGenerator();
                String cardNumber = cardNumberGenerator.createCardNumber();

                if(debitCardRepository.findByCardNumber(cardNumber) != null){
                    continue;
                }

                debitCard.setCardNumber(cardNumber);

                isUnique = false;
            }

            debitCard.setDemandDepositAccount(demandDepositAccount);
            debitCard.setCustomerId(demandDepositAccount.getCustomerId());
            debitCard.setDepositId(demandDepositAccount.getId());

            demandDepositAccount.getDebitCards().add(debitCard);
            demandAccountRepository.save(demandDepositAccount);

            return ResponseEntity.status(HttpStatus.CREATED).body("Debit card is created");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Deposit account is not found");
    }

    public List<DebitCard> findAll() {

        return debitCardRepository.findAll();
    }

    public ResponseEntity<Object> deleteDebitCard(String cardNumber){

        DebitCard debitCard = debitCardRepository.findByCardNumber(cardNumber);

        if(debitCard == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Debit card is not found");
        }

        debitCardRepository.deleteAll(debitCard.getDemandDepositAccount().getDebitCards());

        return ResponseEntity.status(HttpStatus.OK).body("Debit card is deleted");

    }

    public ResponseEntity<Object> addBalance(BalanceTransactionRequest balanceTransactionRequest) throws IOException {

        DebitCard debitCard = debitCardRepository.findByCardNumber(balanceTransactionRequest.getCardNumber());

        if(debitCard != null){

            DemandDepositAccount demandDepositAccount = debitCard.getDemandDepositAccount();
            Customer customer = debitCard.getDemandDepositAccount().getCustomer();

            demandDepositAccount.setBalance(debitCard.getDemandDepositAccount().getBalance() + balanceTransactionRequest.getAmount());

            CardStatement cardStatement = new CardStatement(0L, balanceTransactionRequest.getCardNumber(), new Timestamp(System.currentTimeMillis()),"Add Balance","DebitCard");

            cardStatementRepository.save(cardStatement);

            customerRepository.save(customer);
            demandAccountRepository.save(demandDepositAccount);

            transactionRepository.save(new Transaction(0L, demandDepositAccount.getIbanNo(), demandDepositAccount.getIbanNo(), balanceTransactionRequest.getAmount(), new Timestamp(System.currentTimeMillis()),"Debit Card -> Add Balance"));
            return ResponseEntity.status(HttpStatus.OK).body("Balance is added");

        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Debit card is not found");
    }

    public ResponseEntity<Object> withDrawBalance(BalanceTransactionRequest balanceTransactionRequest) throws IOException{

        DebitCard debitCard = debitCardRepository.findByCardNumber(balanceTransactionRequest.getCardNumber());

        if(debitCard != null){

            DemandDepositAccount demandDepositAccount = debitCard.getDemandDepositAccount();
            Customer customer = debitCard.getDemandDepositAccount().getCustomer();

            if(balanceTransactionRequest.getAmount() > demandDepositAccount.getBalance()){
                return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("Your account balance is not enough to withdraw this money");
            }

            demandDepositAccount.setBalance(debitCard.getDemandDepositAccount().getBalance() - balanceTransactionRequest.getAmount());

            CardStatement cardStatement = new CardStatement(0L, balanceTransactionRequest.getCardNumber(), new Timestamp(System.currentTimeMillis()),"Withdraw Balance","DebitCard");
            cardStatementRepository.save(cardStatement);

            cardStatementRepository.save(cardStatement);

            customerRepository.save(customer);
            demandAccountRepository.save(demandDepositAccount);

            transactionRepository.save(new Transaction(0L, demandDepositAccount.getIbanNo(),demandDepositAccount.getIbanNo(), balanceTransactionRequest.getAmount(), new Timestamp(System.currentTimeMillis()),"Debit Card -> Withdraw Balance"));
            return ResponseEntity.status(HttpStatus.OK).body("Balance has been reduced");
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Debit card is not found");

    }

}
