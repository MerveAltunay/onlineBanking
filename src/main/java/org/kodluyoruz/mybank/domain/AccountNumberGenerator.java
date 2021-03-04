package org.kodluyoruz.mybank.domain;

import java.util.Random;

public class AccountNumberGenerator {

    public String createAccountNumber(){

        Random random = new Random();

        String accountNumber = "";
        int i = 0;

        while (i != 16){

            accountNumber += String.valueOf(random.nextInt(9));
            i++;
        }

        return accountNumber;
    }

}
