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
public class DemandAccountService {

    @Autowired
    private DemandAccountRepository demandAccountRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private SavingsAccountRepository savingsRepository;

    public DemandAccountService(DemandAccountRepository demandAccountRepository, CustomerRepository customerRepository, TransactionRepository transactionRepository, SavingsAccountRepository savingsRepository) {
        this.demandAccountRepository = demandAccountRepository;
        this.customerRepository = customerRepository;
        this.transactionRepository = transactionRepository;
        this.savingsRepository = savingsRepository;
    }

    public DemandAccountService() {

    }

    public ResponseEntity<Object> createDemandAccount(CreateAccountRequest createAccountRequest){

        DemandDepositAccount demandDepositAccount = new DemandDepositAccount();
        Customer customer = customerRepository.findByCustomerId(createAccountRequest.getCustomerId());

        String accountType = createAccountRequest.getAccountType();

        if(!(accountType.equals("EUR")|| accountType.equals("TRY") || accountType.equals("USD"))){
            return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body("Invalid account type");
        }

        if(customer == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Customer is not found");
        }
        else {

            boolean isUnique = true;

            while (isUnique){

                AccountNumberGenerator accountNumberGenerator = new AccountNumberGenerator();
                String accountNumber = accountNumberGenerator.createAccountNumber();

                if(demandAccountRepository.findByAccountNumber(accountNumber) != null){
                    continue;
                }

                IbanGenerator ibanGenerator = new IbanGenerator();
                String iban = ibanGenerator.createIban(accountNumber);

                demandDepositAccount.setBalance(0.0);
                demandDepositAccount.setCustomerId(customer.getCustomerId());
                demandDepositAccount.setCustomer(customer);
                demandDepositAccount.setAccountType(createAccountRequest.getAccountType());
                demandDepositAccount.setAccountNumber(accountNumber);
                demandDepositAccount.setIbanNo(iban);

                isUnique = false;

            }

            customer.getDemandDepositAccounts().add(demandDepositAccount);

            customerRepository.save(customer);

            return ResponseEntity.status(HttpStatus.OK).body("Demand deposit account is created");

        }

    }

    public List<DemandDepositAccount> findAll() {

        return demandAccountRepository.findAll();
    }

    public ResponseEntity<Object> deleteById(long id) {

        Optional<DemandDepositAccount> depositAccount = demandAccountRepository.findById(id);

        if(depositAccount.isPresent()){

            if(depositAccount.get().getBalance() != 0){
                return ResponseEntity.status(HttpStatus.LOCKED).body("Deposit account have money.Please empty your account first.");
            }
            demandAccountRepository.deleteById(id);
            return ResponseEntity.status(HttpStatus.OK).body("Deposit account is deleted");

        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Deposit account is not found");

    }

    public ResponseEntity<Object> sendMoneyToDeposit(TransferRequest transferRequest) throws IOException {

        DemandDepositAccount senderDeposit = demandAccountRepository.findByIbanNo(transferRequest.getFromIbanNo());
        DemandDepositAccount receiverDeposit = demandAccountRepository.findByIbanNo(transferRequest.getToIbanNo());
        double amount = transferRequest.getAmount();

        if(receiverDeposit == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Receiver account not found");
        }
        if(senderDeposit == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Sender account not found");
        }

        if(senderDeposit.getBalance() >= amount){

            String senderType = senderDeposit.getAccountType();
            String receiverType = receiverDeposit.getAccountType();

            Customer senderCustomer = customerRepository.findByCustomerId(senderDeposit.getCustomerId());
            Customer receiverCustomer = customerRepository.findByCustomerId(receiverDeposit.getCustomerId());

            ObjectMapper objectMapper = new ObjectMapper();
            URL url = new URL("https://api.exchangeratesapi.io/latest?base=TRY");
            CurrencyClass currencyClass = objectMapper.readValue(url, CurrencyClass.class);

            double transactionRate = currencyClass.getRates().get(receiverType) / currencyClass.getRates().get(senderType);

            double newSenderBalance = senderDeposit.getBalance() - amount;
            double newReceiverBalance = receiverDeposit.getBalance() + (amount * transactionRate);

            senderDeposit.setBalance(newSenderBalance);
            receiverDeposit.setBalance(newReceiverBalance);

            customerRepository.save(senderCustomer);
            customerRepository.save(receiverCustomer);

            demandAccountRepository.save(senderDeposit);
            demandAccountRepository.save(receiverDeposit);

            transactionRepository.save(new Transaction(0L, transferRequest.getFromIbanNo(), transferRequest.getToIbanNo(), amount, new Timestamp(System.currentTimeMillis()), "Deposit -> Deposit" ));
            return ResponseEntity.status(HttpStatus.OK).body("Transaction completed");

        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Sender don't have money for transaction");
    }

    public ResponseEntity<Object> addBalance(AccountBalanceTransaction balanceTransaction) throws IOException {

        DemandDepositAccount demandDepositAccount = demandAccountRepository.findByAccountNumber(balanceTransaction.getAccountNumber());

        if(demandDepositAccount != null){

            Customer senderCustomer = customerRepository.findByCustomerId(demandDepositAccount.getCustomerId());

            demandDepositAccount.setBalance(demandDepositAccount.getBalance() + balanceTransaction.getAmount());

            demandAccountRepository.save(demandDepositAccount);
            customerRepository.save(senderCustomer);

            transactionRepository.save(new Transaction(0L, balanceTransaction.getAccountNumber(),balanceTransaction.getAccountNumber(), balanceTransaction.getAmount(), new Timestamp(System.currentTimeMillis()),"Deposit -> Add Balance"));
            return ResponseEntity.status(HttpStatus.OK).body("Balance is added");
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Account not found");
    }

    public ResponseEntity<Object> sendMoneyToSavings(TransferRequest transferRequest) throws IOException {

        DemandDepositAccount senderDeposit = demandAccountRepository.findByIbanNo(transferRequest.getFromIbanNo());
        SavingsAccount receiverSavings = savingsRepository.findByIbanNo(transferRequest.getToIbanNo());
        double amount = transferRequest.getAmount();

        if(receiverSavings == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Receiver account not found");
        }

        if(senderDeposit == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Sender account not found");
        }

        if(senderDeposit.getBalance() >= amount){

            String senderType = senderDeposit.getAccountType();
            String receiverType = receiverSavings.getAccountType();

            ObjectMapper objectMapper = new ObjectMapper();
            URL url = new URL("https://api.exchangeratesapi.io/latest?base=TRY");
            CurrencyClass currencyClass = objectMapper.readValue(url,CurrencyClass.class);

            double transactionRate = currencyClass.getRates().get(receiverType) / currencyClass.getRates().get(senderType) ;

            double newSenderBalance = senderDeposit.getBalance() - amount;
            double newReceiverBalance = receiverSavings.getBalance() + (amount * transactionRate);

            senderDeposit.setBalance(newSenderBalance);
            receiverSavings.setBalance(newReceiverBalance);

            demandAccountRepository.save(senderDeposit);
            savingsRepository.save(receiverSavings);

            transactionRepository.save(new Transaction(0L, transferRequest.getFromIbanNo(),transferRequest.getToIbanNo(), amount, new Timestamp(System.currentTimeMillis()),"Deposit -> Savings"));
            return ResponseEntity.status(HttpStatus.OK).body("Transaction completed");
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Sender dont have money for transaction");
    }

    public ResponseEntity<Object> withDrawDeposit(AccountBalanceTransaction balanceTransaction) throws IOException{

        DemandDepositAccount receiverDeposit = demandAccountRepository.findByAccountNumber(balanceTransaction.getAccountNumber());
        double amount = balanceTransaction.getAmount();

        if(receiverDeposit == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Account not found");
        }

        if(receiverDeposit.getBalance() >= amount) {

            double newReceiverDeposit = receiverDeposit.getBalance() - amount;
            receiverDeposit.setBalance(newReceiverDeposit);

            demandAccountRepository.save(receiverDeposit);
            transactionRepository.save(new Transaction(0L, balanceTransaction.getAccountNumber(),balanceTransaction.getAccountNumber(),amount, new Timestamp(System.currentTimeMillis()),"Deposit -> WithDraw"));
            return ResponseEntity.status(HttpStatus.OK).body("Transaction completed");

        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("There is not enough money to withdraw");

    }

}
