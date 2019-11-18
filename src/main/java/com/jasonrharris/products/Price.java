package com.jasonrharris.products;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.jasonrharris.converters.LocalDateTimeConverter;
import com.jasonrharris.orders.OrderItem;

import javax.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.Locale;
import java.util.Objects;

@Entity
public final class Price implements Comparable<Price> {
    static final BigDecimal UNSET_AMOUNT = new BigDecimal("-1");

    @Id
    @SequenceGenerator(name= "PRICE_SEQUENCE", sequenceName = "PRICE_SEQUENCE_ID", initialValue=1, allocationSize = 1)
    @GeneratedValue(strategy=GenerationType.AUTO, generator="PRICE_SEQUENCE")
    private final long id;

    @ManyToOne
    @JoinColumn(name = "product_id")
    @JsonBackReference
    private final Product product;
    @Column
    private final BigDecimal amount;
    @Column
    private final Currency currency;
    @Column
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private final LocalDateTime creationDateTime;

    private Price(long id,Product product, BigDecimal amount, Currency currency) {
        this.id = id;
        this.product = product;
        this.amount = amount;
        this.currency = currency;
        creationDateTime = LocalDateTime.now();
    }

    @SuppressWarnings("unused")
    public
        //default required for Hibernate
    Price() {
        this(0L, new Product(), UNSET_AMOUNT, Currency.getInstance(Locale.getDefault()));
    }

    public static Price createPrice(Product product, String amount, String currency) {
        return createPrice(product, new BigDecimal(amount).setScale(2, RoundingMode.HALF_UP), Currency.getInstance(currency));
    }

    public static Price createPrice(Product product, BigDecimal amount, Currency currency) {
        return new Price(0L, product, amount, currency);
    }

    public long getId() {
        return id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Currency getCurrency() {
        return currency;
    }

    public LocalDateTime getCreationDateTime() {
        return creationDateTime;
    }

    public Product getProduct() {
        return product;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Price price = (Price) o;
        return
                (id == 0L && amount.equals(price.amount)
                        &&
                        currency.equals(price.currency)
                        && Objects.equals(product, price.product)
                        || (id != 0L && id == price.id));
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Price{" +
                "id=" + id +
                ", product=" + product +
                ", amount=" + amount +
                ", currency=" + currency +
                '}';
    }

    @Override
    public int compareTo(Price price) {
        if (!product.equals(price.product)){
            return product.getName().compareTo(price.getProduct().getName());
        }
        if (currency.equals(price.currency)) {
            return amount.compareTo(price.amount);
        } else {
            return currency.toString().compareTo(price.currency.toString());
        }
    }

    @JsonIgnore
    public boolean isUnset() {
        return this.amount.compareTo(UNSET_AMOUNT) <= 0;
    }
}
