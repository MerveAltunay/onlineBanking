package org.kodluyoruz.mybank.helper;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DebtOnAccount {

    private String creditCardNumber;

    private double amount;

    private String accountNumber;
}
