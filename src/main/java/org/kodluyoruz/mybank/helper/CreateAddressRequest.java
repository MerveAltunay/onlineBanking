package org.kodluyoruz.mybank.helper;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateAddressRequest {

    private String city;

    private String street;

    private String houseNumber;

    private String zipCode;
}
