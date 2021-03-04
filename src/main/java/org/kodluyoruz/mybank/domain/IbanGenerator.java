package org.kodluyoruz.mybank.domain;

import java.util.Random;

public class IbanGenerator {

    public String createIban(String accountNumber){

        String iban = "TR";
        Random random = new Random();

        for(int i=0; i<2; i++){

            iban += String.valueOf(random.nextInt(9));
        }

        iban += "00001";

        iban += accountNumber;

        return iban;
    }
}
