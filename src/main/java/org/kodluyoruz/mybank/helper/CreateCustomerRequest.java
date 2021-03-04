package org.kodluyoruz.mybank.helper;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateCustomerRequest {

    private String citizenshipNumber;

    private String name;

    private String surname;

    private String email;

    private long phoneNumber;

    private CreateAddressRequest address;

}
