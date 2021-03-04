package org.kodluyoruz.mybank.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.kodluyoruz.mybank.entity.Customer;
import org.kodluyoruz.mybank.entity.account.DemandDepositAccount;
import org.kodluyoruz.mybank.entity.account.SavingsAccount;
import org.kodluyoruz.mybank.entity.transaction.Transaction;
import org.kodluyoruz.mybank.domain.AccountNumberGenerator;
import org.kodluyoruz.mybank.domain.CurrencyClass;
import org.kodluyoruz.mybank.domain.IbanGenerator;
import org.kodluyoruz.mybank.repository.CustomerRepository;
import org.kodluyoruz.mybank.repository.DemandAccountRepository;
import org.kodluyoruz.mybank.repository.SavingsAccountRepository;
import org.kodluyoruz.mybank.repository.TransactionRepository;
import org.kodluyoruz.mybank.helper.AccountBalanceTransaction;
import org.kodluyoruz.mybank.helper.CreateAccountRequest;
import org.kodluyoruz.mybank.helper.TransferRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Service
public class SavingsAccountService {

    @Autowired
    private SavingsAccountRepository savingsAccountRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private DemandAccountRepository demandAccountRepository;

    public SavingsAccountService(SavingsAccountRepository savingsAccountRepository, CustomerRepository customerRepository, TransactionRepository transactionRepository, DemandAccountRepository demandAccountRepository) {
        this.savingsAccountRepository = savingsAccountRepository;
        this.customerRepository = customerRepository;
        this.transactionRepository = transactionRepository;
        this.demandAccountRepository = demandAccountRepository;
    }

    public SavingsAccountService() {

    }

    public ResponseEntity<Object> createSavingsAccount(CreateAccountRequest createAccountRequest) {

        SavingsAccount savingsAccount = new SavingsAccount();
        Customer customer = customerRepository.findByCustomerId(createAccountRequest.getCustomerId());

        String accountType = createAccountRequest.getAccountType();

        if (!(accountType.equals("EUR") || accountType.equals("TRY") || accountType.equals("USD"))) {
            return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body("Invalid account type");
        }

        if (customer == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Customer is not found");
        }

        else {

            boolean isUnique = true;

            while (isUnique) {

                AccountNumberGenerator accountNumberGenerator = new AccountNumberGenerator();
                String accountNumber = accountNumberGenerator.createAccountNumber();

                if (savingsAccountRepository.findByAccountNumber(accountNumber) != null) {
                    continue;
                }

                IbanGenerator ibanGenerator = new IbanGenerator();
                String iban = ibanGenerator.createIban(accountNumber);

                savingsAccount.setBalance(0.0);
                savingsAccount.setCustomerId(customer.getCustomerId());
                savingsAccount.setCustomer(customer);
                savingsAccount.setAccountType(createAccountRequest.getAccountType());
                savingsAccount.setAccountNumber(accountNumber);
                savingsAccount.setIbanNo(iban);

                isUnique = false;

            }

            customer.getSavingsAccounts().add(savingsAccount);

            customerRepository.save(customer);

            return ResponseEntity.status(HttpStatus.OK).body("Savings account is created");

        }
    }

    public List<SavingsAccount> findAll() {

        return savingsAccountRepository.findAll();
    }

    public ResponseEntity<Object> sendMoneyToDeposit(TransferRequest transferRequest) throws IOException {

        SavingsAccount senderSavings = savingsAccountRepository.findByIbanNo(transferRequest.getFromIbanNo());
        DemandDepositAccount receiverDeposit = demandAccountRepository.findByIbanNo(transferRequest.getToIbanNo());
        double amount = transferRequest.getAmount();

        if(receiverDeposit == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Receiver account not found");
        }

        if(senderSavings == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Sender account not found");
        }

        if(senderSavings.getCustomerId() != receiverDeposit.getCustomerId()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Savings account just send own account");
        }

        if(senderSavings.getBalance() >= amount){

            String senderType = senderSavings.getAccountType();
            String receiverType = receiverDeposit.getAccountType();

            ObjectMapper objectMapper = new ObjectMapper();
            URL url = new URL("https://api.exchangeratesapi.io/latest?base=TRY");
            CurrencyClass currencyClass = objectMapper.readValue(url, CurrencyClass.class);

            double transactionRate = currencyClass.getRates().get(receiverType) / currencyClass.getRates().get(senderType);

            double newSenderBalance = senderSavings.getBalance() - amount;
            double newReceiverBalance = receiverDeposit.getBalance() + (amount * transactionRate);

            senderSavings.setBalance(newSenderBalance);
            receiverDeposit.setBalance(newReceiverBalance);

            savingsAccountRepository.save(senderSavings);
            demandAccountRepository.save(receiverDeposit);

            transactionRepository.save(new Transaction(0L, transferRequest.getFromIbanNo(), transferRequest.getToIbanNo(), amount, new Timestamp(System.currentTimeMillis()),"Savings -> Deposit"));
            return ResponseEntity.status(HttpStatus.OK).body("Transaction completed");

        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Sender dont have money for transaction");
    }

    public ResponseEntity<Object> addBalance(AccountBalanceTransaction balanceTransaction) {

        SavingsAccount savingsAccount = savingsAccountRepository.findByAccountNumber(balanceTransaction.getAccountNumber());

        if(savingsAccount != null){

            savingsAccount.setBalance(savingsAccount.getBalance() + balanceTransaction.getAmount());
            savingsAccountRepository.save(savingsAccount);
            transactionRepository.save(new Transaction(0L, balanceTransaction.getAccountNumber(), balanceTransaction.getAccountNumber(), balanceTransaction.getAmount(), new Timestamp(System.currentTimeMillis()),"Savings -> Add Balance"));
            return ResponseEntity.status(HttpStatus.OK).body("Balance is added");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Account not found");
    }

    public ResponseEntity<Object> sendMoneyToSavings(TransferRequest transferRequest) throws IOException {

        SavingsAccount senderSavings = savingsAccountRepository.findByIbanNo(transferRequest.getFromIbanNo());
        SavingsAccount receiverSavings = savingsAccountRepository.findByIbanNo(transferRequest.getToIbanNo());
        double amount = transferRequest.getAmount();

        if(receiverSavings == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Receiver account not found");
        }

        if(senderSavings == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Sender account not found");
        }

        if(senderSavings.getCustomerId() != receiverSavings.getCustomerId()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Savings account just send own account");
        }

        if(senderSavings.getBalance() >= amount){

            String senderType = senderSavings.getAccountType();
            String receiverType = receiverSavings.getAccountType();

            ObjectMapper objectMapper =new ObjectMapper();
            URL url = new URL("https://api.exchangeratesapi.io/latest?base=TRY");
            CurrencyClass currencyClass =objectMapper.readValue(url,CurrencyClass.class);

            double transactionRate = currencyClass.getRates().get(receiverType) / currencyClass.getRates().get(senderType) ;

            double newSenderBalance = senderSavings.getBalance() - amount;
            double newReceiverBalance = receiverSavings.getBalance() + (amount * transactionRate);

            senderSavings.setBalance(newSenderBalance);
            receiverSavings.setBalance(newReceiverBalance);

            savingsAccountRepository.save(senderSavings);
            savingsAccountRepository.save(receiverSavings);

            transactionRepository.save(new Transaction(0L, transferRequest.getFromIbanNo(),transferRequest.getToIbanNo(), amount, new Timestamp(System.currentTimeMillis()),"Savings -> Savings"));
            return ResponseEntity.status(HttpStatus.OK).body("Transaction completed");
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Sender dont have money for transaction");
    }

    public ResponseEntity<Object> deleteById(long id) {

        Optional<SavingsAccount> savingsAccount = savingsAccountRepository.findById(id);

        if(savingsAccount.isPresent()){

            if(savingsAccount.get().getBalance() != 0){
                return ResponseEntity.status(HttpStatus.LOCKED).body("Savings account have money.Please empty your account first");
            }
            savingsAccountRepository.deleteById(id);
            return ResponseEntity.status(HttpStatus.OK).body("Savings account is deleted");

        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Savings account is not found");
    }

    public ResponseEntity<Object> withDrawDeposit(AccountBalanceTransaction balanceTransaction) throws IOException{

        SavingsAccount receiverSaving = savingsAccountRepository.findByAccountNumber(balanceTransaction.getAccountNumber());
        double amount = balanceTransaction.getAmount();

        if(receiverSaving == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Account not found");
        }

        if(receiverSaving.getBalance() >= amount) {

            double newReceiverDeposit = receiverSaving.getBalance() - amount;
            receiverSaving.setBalance(newReceiverDeposit);

            savingsAccountRepository.save(receiverSaving);
            transactionRepository.save(new Transaction(0L, balanceTransaction.getAccountNumber(),balanceTransaction.getAccountNumber(),amount, new Timestamp(System.currentTimeMillis()),"Saving -> WithDraw"));
            return ResponseEntity.status(HttpStatus.OK).body("Transaction completed");

        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("There is not enough money to withdraw");

    }

}
