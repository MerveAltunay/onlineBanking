package org.kodluyoruz.mybank.controller;

import org.kodluyoruz.mybank.entity.account.SavingsAccount;
import org.kodluyoruz.mybank.helper.AccountBalanceTransaction;
import org.kodluyoruz.mybank.helper.CreateAccountRequest;
import org.kodluyoruz.mybank.helper.TransferRequest;
import org.kodluyoruz.mybank.service.SavingsAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/customer/account/savings")
public class SavingsAccountController {

    @Autowired
    private SavingsAccountService savingsAccountService;

    @PostMapping("/create")
    ResponseEntity<Object> createSavingsAccount(@RequestBody CreateAccountRequest createAccountRequest) {

        return savingsAccountService.createSavingsAccount(createAccountRequest);

    }

    @GetMapping("/findAll")
    public List<SavingsAccount> findAll(){

        return savingsAccountService.findAll();
    }

    @PostMapping("/send/deposit")
    public ResponseEntity<Object> sendMoneyToDeposit(@RequestBody TransferRequest request) throws IOException {

        return savingsAccountService.sendMoneyToDeposit(request);
    }

    @PostMapping("/addBalance")
    public ResponseEntity<Object> addBalance(@RequestBody AccountBalanceTransaction request) throws IOException {

        return savingsAccountService.addBalance(request);
    }

    @PostMapping("/send/savings")
    public ResponseEntity<Object> sendMoneyToSavings(@RequestBody TransferRequest request) throws IOException {

        return savingsAccountService.sendMoneyToSavings(request);
    }

    @DeleteMapping("/deleteById/{id}}")
    public ResponseEntity<Object> deleteById(@PathVariable long id){

        return savingsAccountService.deleteById(id);
    }

    @PostMapping("/withdraw")
    public ResponseEntity<Object> withdraw(@RequestBody AccountBalanceTransaction transferRequest) throws IOException {

        return savingsAccountService.withDrawDeposit(transferRequest);
    }

}
