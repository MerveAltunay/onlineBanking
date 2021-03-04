package org.kodluyoruz.mybank.controller;

import org.kodluyoruz.mybank.entity.card.CreditCard;
import org.kodluyoruz.mybank.entity.transaction.CardStatement;
import org.kodluyoruz.mybank.repository.CardStatementRepository;
import org.kodluyoruz.mybank.helper.BalanceTransactionRequest;
import org.kodluyoruz.mybank.helper.CreditCardRequest;
import org.kodluyoruz.mybank.helper.DebtOnAccount;
import org.kodluyoruz.mybank.helper.DebtOnDebitRequest;
import org.kodluyoruz.mybank.service.CreditCardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/customer/creditCard")
public class CreditCardController {

    @Autowired
    private CreditCardService creditCardService;

    @Autowired
    private CardStatementRepository cardStatementRepository;

    @PostMapping("/create")
    ResponseEntity<Object> createCreditCard(@RequestBody CreditCardRequest creditCardRequest){

        return creditCardService.createCreditCard(creditCardRequest);
    }

    @GetMapping("/findAll")
    public List<CreditCard> findAll(){

        return creditCardService.findAll();
    }

    @PostMapping("/withDrawBalance")
    public ResponseEntity<Object> withDrawBalance(@RequestBody BalanceTransactionRequest balanceTransactionRequest) throws Exception {


            return creditCardService.withDrawCreditCard(balanceTransactionRequest);



    }

    @GetMapping("/debtInquiry/{creditCardNumber}")
    public ResponseEntity<Object> debtInquiry(@PathVariable String creditCardNumber) throws IOException {

        return creditCardService.debtInquiry(creditCardNumber);
    }

    @PostMapping("/debtOnAccount")
    public ResponseEntity<Object> debtOnAccount(@RequestBody DebtOnAccount debtOnAccount) throws IOException {

        return creditCardService.debtOnAccount(debtOnAccount);

    }

    @PostMapping("/debtOnDebit")
    public ResponseEntity<Object> debtOnDebit(@RequestBody DebtOnDebitRequest request) throws IOException {

        return creditCardService.debtOnDebitCard(request);

    }

    @GetMapping("/getStatement({cardNumber}")
    public List<CardStatement> getStatement(@PathVariable String cardNumber){

        return cardStatementRepository.findByCardNumber(cardNumber);
    }

    @DeleteMapping("/delete/{cardNumber}")
    public ResponseEntity<Object> deleteCreditCard(@PathVariable String cardNumber){

        return creditCardService.deleteCreditCard(cardNumber);
    }
}
