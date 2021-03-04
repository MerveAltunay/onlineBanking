package org.kodluyoruz.mybank.service;

import org.kodluyoruz.mybank.entity.transaction.Transaction;
import org.kodluyoruz.mybank.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    public List<Transaction> findAll() {

        return transactionRepository.findAll();
    }

}
