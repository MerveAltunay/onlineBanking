package org.kodluyoruz.mybank.controller;

import org.kodluyoruz.mybank.entity.card.DebitCard;
import org.kodluyoruz.mybank.helper.BalanceTransactionRequest;
import org.kodluyoruz.mybank.helper.DebitCardRequest;
import org.kodluyoruz.mybank.service.DebitCardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/customer/account/deposit/debitCard")
public class DebitCardController {

    @Autowired
    private DebitCardService debitCardService;


    @PostMapping("/create")
    public ResponseEntity<Object> createDebitCard(@RequestBody DebitCardRequest debitCardRequest){
        return debitCardService.createDebitCard(debitCardRequest);
    }

    @GetMapping("/findAll")
    public List<DebitCard> findAll(){

        return debitCardService.findAll();
    }

    @PostMapping("/addBalance")
    public ResponseEntity<Object> addBalance(@RequestBody BalanceTransactionRequest balanceTransactionRequest) throws IOException {

        return debitCardService.addBalance(balanceTransactionRequest);

    }

    @PostMapping("/withDrawBalance")
    public ResponseEntity<Object> withDrawBalance(@RequestBody BalanceTransactionRequest balanceTransactionRequest) throws IOException {

        return debitCardService.withDrawBalance(balanceTransactionRequest);

    }

    @DeleteMapping("/deleteDebitCard/{cardNumber}")
    ResponseEntity<Object> deleteByCardNumber(@PathVariable String cardNumber){

        return debitCardService.deleteDebitCard(cardNumber);
    }

}
