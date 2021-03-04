package org.kodluyoruz.mybank.helper;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreditCardRequest {

    private double cardLimit;

    private long customerId;
}
