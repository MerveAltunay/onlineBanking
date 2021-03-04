package org.kodluyoruz.mybank.controller;

import org.kodluyoruz.mybank.entity.transaction.Transaction;
import org.kodluyoruz.mybank.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/customer/transaction")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @GetMapping("/findAll")
    List<Transaction> findAll(){

        return transactionService.findAll();
    }
}
