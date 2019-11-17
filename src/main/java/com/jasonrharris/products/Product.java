package com.jasonrharris.products;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinFormula;

import java.util.Objects;
import java.util.Optional;
import javax.persistence.*;

@Entity
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column
    private final String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinFormula("(" +
            "SELECT pc.id " +
            "FROM Price pc " +
            "WHERE pc.product_id = id " +
            "ORDER BY pc.creation_date_time DESC " +
            "LIMIT 1" +
            ")")
    private final Price currentPrice;

    @SuppressWarnings("unused") //needed by Hibernate
    public Product() {
        this("");
    }

    public Product(String name) {
        this(0L, name, null);
    }

    public Product(long id, String name, Price currentPrice) {
        this.id = id;
        this.name = name;
        this.currentPrice = currentPrice;
    }

    public String getName() {
        return name;
    }

    public Optional<Price> getCurrentPrice() {
        return Optional.ofNullable(currentPrice);
    }

    public long getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return (id == 0L && name.equals(product.name) || id == product.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + currentPrice +
                '}';
    }
}
