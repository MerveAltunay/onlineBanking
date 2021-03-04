package org.kodluyoruz.mybank.entity.card;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.kodluyoruz.mybank.entity.account.DemandDepositAccount;

import javax.persistence.*;

@Entity
@Getter
@Setter
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DebitCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private long customerId;
    private long depositId;
    private String cardNumber;

    @ManyToOne
    @JsonIgnore
    private DemandDepositAccount demandDepositAccount;

}