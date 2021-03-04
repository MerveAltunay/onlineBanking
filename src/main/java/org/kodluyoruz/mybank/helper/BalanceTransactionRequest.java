package org.kodluyoruz.mybank.helper;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BalanceTransactionRequest {

    private String cardNumber;

    private double amount;
}
