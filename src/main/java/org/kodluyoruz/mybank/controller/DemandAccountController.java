package org.kodluyoruz.mybank.controller;

import org.kodluyoruz.mybank.entity.account.DemandDepositAccount;
import org.kodluyoruz.mybank.helper.AccountBalanceTransaction;
import org.kodluyoruz.mybank.helper.CreateAccountRequest;
import org.kodluyoruz.mybank.helper.TransferRequest;
import org.kodluyoruz.mybank.service.DemandAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/customer/account/demand")
public class DemandAccountController {

    @Autowired
    private DemandAccountService demandAccountService;

    @PostMapping("/create")
    ResponseEntity<Object> createDemandAccount(@RequestBody CreateAccountRequest createAccountRequest){

        return demandAccountService.createDemandAccount(createAccountRequest);
    }

    @GetMapping("findAll")
    public List<DemandDepositAccount> findAll(){

        return demandAccountService.findAll();
    }

    @PostMapping("/send/deposit")
    public ResponseEntity<Object> sendMoneyToDeposit(@RequestBody TransferRequest transferRequest) throws IOException {

        return demandAccountService.sendMoneyToDeposit(transferRequest);
    }

    @PostMapping("/addBalance")
    public ResponseEntity<Object> addBalance(@RequestBody AccountBalanceTransaction balanceTransaction) throws IOException {

        return demandAccountService.addBalance(balanceTransaction);
    }

    @PostMapping("/send/savings")
    public ResponseEntity<Object> sendMoneyToSavings(@RequestBody TransferRequest transferRequest) throws IOException {

        return demandAccountService.sendMoneyToSavings(transferRequest);
    }

    @PostMapping("/withdraw")
    public ResponseEntity<Object> withdraw(@RequestBody AccountBalanceTransaction transferRequest) throws IOException {

        return demandAccountService.withDrawDeposit(transferRequest);
    }

    @DeleteMapping("/deleteById/{id}}")
    public ResponseEntity<Object> deleteById(@PathVariable long id){

        return demandAccountService.deleteById(id);
    }
}
