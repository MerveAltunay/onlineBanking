

# *Online Bankacılık Sistemi* 

Java 8 kullanılarak **müşteri, hesap, kart** ve **transfer** yönetiminin olduğu bir online bankacılık sisteminin backend servisleri yazılmıştır. 

Projede tüm fonksiyonlar REST API'lar ile yerine getirilmiştir. Spring framework tabanlı Spring Boot ve veri tabanı işlemleri için JPA kullanılmıştır.

*İlk olarak entity sınıfları oluşturularak, bunlar arasındaki ilişkiler tanımlanmıştır. Bu sınıfların değişkenlerini ve aralarındaki ilişkileri aşağıdaki ER diyagramında görmekteyiz.*

![mybank](https://user-images.githubusercontent.com/76403911/107887420-055f6180-6f17-11eb-8ddc-c673075e9437.jpeg)



*Bütün Entity sınıflarımızın, verilere eriştiğimiz Repository'leri birer interface olarak oluşturuldu.*

- Müşterileri id ye göre listeleme
- Müşterileri vatandaşlık numarasına göre listeleme
- Müşterilerin adreslerini şehirlerine göre listeleme
- Iban ve hesap numaralarına göre müşterilerin hesaplarını listeleme
- Kart numarasına göre müşterilerin kartlarını listeleme
- Kart numarasına göre ekstre listeleme

*İşlemleri yöneteceğimiz Service'ler yazıldı.*
## Müşteri Yönetimi

Müşteri yaratma, güncelleme ve silme işlemleri eklendi. Her işlem içerisinde müşterinin olup olmadığı kontrol edildi. Eğer müşteri yoksa "Müşteri bulunamadı" şeklinde mesaj döndürüldü. 

```java
public ResponseEntity<Object> createCustomer(CreateCustomerRequest customerRequest){

    String number = String.valueOf(customerRequest.getCitizenshipNumber());

    Customer customer = customerRepository.findByCitizenshipNumber(customerRequest.getCitizenshipNumber());

    if(customer != null){
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body("There is a customer with this number.You can't register again");
    }
```

Müşteriyi yaratırken Customer entity'sinde değişkenlerin hepsi girilmek istenmediğinden **helper** isimli bir paket altına CreateCustomerRequest isimli sınıf oluşturarak burada sadece girmek istenilen değişkenler yazıldı. createCustomer fonksiyonuna da parametre olarak bu sınıf verildi.

```java
public ResponseEntity<Object> updateCustomerAddress(CreateAddressRequest request, long citizenshipNumber){

    Customer customer = customerRepository.findByCitizenshipNumber(citizenshipNumber);

    if(customer == null){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Customer is not found");
    }
```

Aynı şekilde adres bilgilerinde istenilen değişkenler için yine helper paketi altında createAddressRequest sınıfı oluşturuldu. updateCustomerAddress fonksiyonu parametre olarak bu sınıfı aldı.

Silme işleminde müşterinin bütün hesapları ve kredi kartları kontrol edilerek eğer hesabında para bulunuyor veya kredi kartı borcu varsa bu müşterilerin silinmesine izin verilmedi. Buna uygun cevaplar oluşturuldu.

```java
for(int i=0; i<deposits.size(); i++)
{
    if(deposits.get(i).getBalance() != 0){
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("You have balance in your account.Not deleted customer");
    }
}

for(int i=0; i<savings.size(); i++)
{
    if(savings.get(i).getBalance() != 0){
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("You have balance in your account.Not deleted customer");
    }
}

List<CreditCard> creditCards = customer.getCreditCards();

for(int i=0; i<creditCards.size(); i++)
{
    if(creditCards.get(i).getCardLimit() - creditCards.get(i).getRemainingCreditLimit() != 0){
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("You have credit debt.Not deleted customer");
    }
```
## Hesap Yönetimi

### Vadesiz Mevduat Hesabı

Hesap yaratma işleminde, girilecek değerler hesabın türü ve hangi müşteriye eklenecek olmasıdır. Bu nedenle helper paketi altında CreateAccountRequest isimli bir sınıf oluşturuldu. createDemandAccount fonksiyonuna parametre olarak bu sınıf verildi.

Silme işleminde hesabın olup olmadığı kontrol edildi. Ayrıca id'ye göre alınan hesabın içerisinde para bulunuyorsa bu hesabın silinmesine izin verilmedi. 

```java
public ResponseEntity<Object> deleteById(long id) {

    Optional<DemandDepositAccount> depositAccount = demandAccountRepository.findById(id);

    if(depositAccount.isPresent()){

        if(depositAccount.get().getBalance() != 0){
            return ResponseEntity.status(HttpStatus.LOCKED).body("Deposit account have money.Please empty your account first.");
        }
        demandAccountRepository.deleteById(id);
        return ResponseEntity.status(HttpStatus.OK).body("Deposit account is deleted");

    }
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Deposit account is not found");

}
```
Vadesiz mevduat hesabı servisine vadesiz mevduat hesabına ve birikim hesabına para gönderme fonksiyonları oluşturuldu. Para transferlerinde gönderen ve alıcının ibanı ve ne kadar gönderileceği gereklidir. Bu nedenle helper paketi altında TransferRequest isimli sınıf oluşturuldu. Bu sınıf para gönderme fonksiyonlarına parametre olarak verildi. 

Hesaba para ekleme ve çekme işleminde hesap numarası ve ne kadar eklenip çekileceği gereklidir. Bu nedenle yine helper paketi altında AccountBalanceTransaction isimli sınıf oluşturuldu.

Tüm işlemlerde hesapların olup olmadığı kontrol edildi. Hesaptan para çekerken, istenilen tutar hesaptaki tutardan fazlaysa para çekme işlemi gerçekleştirilmedi.

```java
public ResponseEntity<Object> withDrawDeposit(AccountBalanceTransaction balanceTransaction) throws IOException{

    DemandDepositAccount receiverDeposit = demandAccountRepository.findByAccountNumber(balanceTransaction.getAccountNumber());
    double amount = balanceTransaction.getAmount();

    if(receiverDeposit == null){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Account not found");
    }

    if(receiverDeposit.getBalance() >= amount) {

        double newReceiverDeposit = receiverDeposit.getBalance() - amount;
        receiverDeposit.setBalance(newReceiverDeposit);

        demandAccountRepository.save(receiverDeposit);
        transactionRepository.save(new Transaction(0L, balanceTransaction.getAccountNumber(),balanceTransaction.getAccountNumber(),amount, new Timestamp(System.currentTimeMillis()),"Deposit -> WithDraw"));
        return ResponseEntity.status(HttpStatus.OK).body("Transaction completed");

    }

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("There is not enough money to withdraw");

}
```
### Birikim Hesabı

Vadesiz mevduat hesabıyla tek fark olarak birikim hesabından başka hesaplara para transferi yapılamayacak şekilde oluşturuldu. Bu işlem aşağıdaki kodla sağlandı. Gönderen hesap ile alıcı hesap aynı müşteriye ait değilse "Savings account just send own account" mesajı döndürüldü.

```java
if(senderSavings.getCustomerId() != receiverDeposit.getCustomerId()){
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Savings account just send own account");
}
```

Hem vadesiz mevduat hem de birikim hesaplarının EUR, USD ve TRY para birimlerinde yaratılması sağlandı. Eğer farklı bir hesap türü girilirse bunun kontrolü de projeye eklendi.

```java
String accountType = createAccountRequest.getAccountType();

if (!(accountType.equals("EUR") || accountType.equals("TRY") || accountType.equals("USD"))) {
    return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body("Invalid account type");
}
```
Hesaplar oluşturulurken gerekli olan iban, hesap numarası gibi bilgiler için **domain** paketi altında IbanGenerator ve AccountNumberGenerator sınıfları oluşturuldu. Bu sınıflarda bu bilgiler generate edildi.

```java
package org.kodluyoruz.mybank.domain;

import java.util.Random;

public class AccountNumberGenerator {

    public String createAccountNumber(){

        Random random = new Random();

        String accountNumber = "";
        int i = 0;

        while (i != 16){

            accountNumber += String.valueOf(random.nextInt(9));
            i++;
        }

        return accountNumber;
    }
}
```
Örneğin, AccountNumberGenerator sınıfına baktığımızda Random olarak sayılar üreterek bir hesap numarası oluşturuldu.
## Kart Yönetimi

### Ön Ödemeli Kart

Kart yaratma işleminde, sadece hesap numarası olması yeterlidir. Hesap numarası girilerek o hesaba ait bir kart oluşturulacaktır. Bu nedenle helper paketi altında DebitCardRequest isimli sınıf oluşturuldu. Bu sınıf createDebitCard fonksiyonuna parametre olarak gönderildi. Ön ödemeli kart vadesiz hesaba bağlı olacağı için hesabın olup olmadığı kontrol edildi. 

Karta para ekleme ve karttan para çekme işleminde sadece kart numarası ve ne kadar eklenip çekileceği bilgisi gerektiğinden helper paketi altında BalanceTransactionRequest sınıfı oluşturularak addBalance fonksiyonuna parametre olarak verildi.

### Kredi Kartı

Kredi kartı yaratma işleminde, sadece kredi kartının limiti ve hangi müşteriye eklenecek bilgisi yeterlidir. Bu nedenle helper paketi altında CreditCardRequest isimli sınıf oluşturuldu. Bu sınıf createCreditCard fonksiyonuna parametre olarak gönderildi. Kredi kartı direk olarak müşteriye bağlı olacağı için müşterinin olup olmadığı kontrol edildi. 

Kredi kartı için borç sorgulama, hesap veya ön ödemeli kart üzerinden borç ödeme fonksiyonları eklendi.

Kredi kartı borcu olan kartların silinmesine izin verilmedi.

```java
double debt = creditCard.getCardLimit() - creditCard.getRemainingCreditLimit();

        if(debt != 0){

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Credit card have a debt");
        }
```
## Transfer Yönetimi

Farklı para birimlerinde açılan hesaplar arası para transferlerinde aşağıdaki kod parçası kullanıldı.

```java
ObjectMapper objectMapper = new ObjectMapper();
            URL url = new URL("https://api.exchangeratesapi.io/latest?base=TRY");
            CurrencyClass currencyClass = objectMapper.readValue(url,CurrencyClass.class);

            double transactionRate = currencyClass.getRates().get(receiverType) / currencyClass.getRates().get(senderType) ;
```

Currency sınıfı, API da bulunan json tipindeki değişkenleri eşlediğimiz sınıftır. @JsonProperty anotasyonu alan json'a çevrilirken kullanılacak tag ismini belirtmektedir.

```java
package org.kodluyoruz.mybank.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.Date;
import java.util.Map;

@Getter
@Setter
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CurrencyClass {

    @JsonProperty("date")
    private Date date;

    @JsonProperty("base")
    private String base;

    @JsonProperty("rates")
    private Map<String, Double> rates;
}
```
*En son olarak requestleri yakaladığımız Controller sınıfları oluşturuldu.*

Kredi kartında ekstre görüntüleme CreditCartController sınıfına eklendi.

```java
@GetMapping("/getStatement({cardNumber}")
    public List<CardStatement> getStatement(@PathVariable String cardNumber){

        return cardStatementRepository.findByCardNumber(cardNumber);
    }
```
CardStatement entity'sindeki bilgiler dönülerek kart ekstresi oluşturulmuştur.
