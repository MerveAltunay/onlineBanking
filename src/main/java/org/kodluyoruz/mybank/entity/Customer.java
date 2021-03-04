package org.kodluyoruz.mybank.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.kodluyoruz.mybank.entity.account.DemandDepositAccount;
import org.kodluyoruz.mybank.entity.account.SavingsAccount;
import org.kodluyoruz.mybank.entity.card.CreditCard;

import javax.persistence.*;
import java.util.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long customerId;

    private String citizenshipNumber;
    private String name;
    private String surname;
    private String email;
    private long phoneNumber;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "address_id",referencedColumnName = "id")
    private Address address;

    @OneToMany(cascade = CascadeType.ALL)
    private List<CreditCard> creditCards;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "demand_id", referencedColumnName = "customerId")
    private List<DemandDepositAccount> demandDepositAccounts;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "savings_id", referencedColumnName = "customerId")
    private List<SavingsAccount> savingsAccounts;

}
