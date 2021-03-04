package org.kodluyoruz.mybank.helper;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DebtOnDebitRequest {

    private String debitCardNumber;

    private double amount;

    private String creditCardNumber;
}
