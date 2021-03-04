package org.kodluyoruz.mybank.helper;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountBalanceTransaction {

    private String accountNumber;

    private double amount;
}
