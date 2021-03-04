package org.kodluyoruz.mybank.entity.account;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.kodluyoruz.mybank.entity.Customer;
import org.kodluyoruz.mybank.entity.card.DebitCard;

import javax.persistence.*;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DemandDepositAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String accountType;
    private double balance;
    private String accountNumber;
    private String ibanNo;
    private long customerId;

    @ManyToOne
    @JsonIgnore
    private Customer customer;

    @OneToMany(cascade = CascadeType.ALL,fetch = FetchType.LAZY)
    private List<DebitCard> debitCards;

}
