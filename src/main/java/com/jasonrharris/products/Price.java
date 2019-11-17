package com.jasonrharris.products;

import com.fasterxml.jackson.annotation.JsonBackReference;

import javax.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

@Entity
public final class Price implements Comparable<Price> {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id = 0L;

    @ManyToOne
    @JoinColumn(name = "product_id")
    @JsonBackReference
    private final Product product;
    @Column
    private final BigDecimal amount;
    @Column
    private final Currency currency;
    @Temporal(TemporalType.TIMESTAMP)
    private
    Date creationDateTime;

    private Price(Product product, BigDecimal amount, Currency currency) {
        this.product = product;
        this.amount = amount;
        this.currency = currency;
        creationDateTime = new Date();
    }

    @SuppressWarnings("unused")
        //default required for Hibernate
    Price() {
        this(new Product(), BigDecimal.ZERO, Currency.getInstance(Locale.getDefault()));
    }

    public static Price createPrice(Product product, String amount, String currency) {
        return createPrice(product, new BigDecimal(amount).setScale(2, RoundingMode.HALF_UP), Currency.getInstance(currency));
    }

    static Price createPrice(Product product, BigDecimal amount, Currency currency) {
        return new Price(product, amount, currency);
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

    public Date getCreationDateTime() {
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
                        && product.equals(price.product))
                        || (id != 0L && id == price.id);
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
}
