package org.kodluyoruz.mybank.helper;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransferRequest {

    private String fromIbanNo;

    private String toIbanNo;

    private double amount;
}
