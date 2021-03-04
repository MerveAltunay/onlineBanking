package org.kodluyoruz.mybank.domain;

import java.util.Random;

public class CardNumberGenerator {

    Random random = new Random();

    public String createCardNumber(){

        String cardNumber = "";

        for(int i=0; i<16; i++){

            int number = random.nextInt(10);

            cardNumber += Integer.toString(number);
        }

        return cardNumber;
    }

}
