package org.kodluyoruz.mybank.entity.card;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.kodluyoruz.mybank.entity.Customer;

import javax.persistence.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreditCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long creditCardId;

    private String cardNumber;
    private double cardLimit;
    private double remainingCreditLimit;
    private long customerId;

    @ManyToOne
    @JsonIgnore
    private Customer customer;

}

