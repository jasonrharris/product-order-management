package com.jasonrharris.orders;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jasonrharris.products.Price;
import com.jasonrharris.products.Product;
import org.aspectj.weaver.ast.Or;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Objects;

@Entity
public class OrderItem {
    private static final int UNSET_QUANTITY = -1;

    @Id
    @SequenceGenerator(name= "ORDER_ITEM_SEQUENCE", sequenceName = "ORDER_ITEM_SEQUENCE_ID", initialValue=1, allocationSize = 1)
    @GeneratedValue(strategy=GenerationType.AUTO, generator="ORDER_ITEM_SEQUENCE")
    private final long id;

    @OneToOne
    private final Price price;

    @OneToOne
    private final Product product;

    @Column
    private final int quantity;

    @ManyToOne
    @JoinColumn(name = "order_id")
    @JsonBackReference
    private final Order parentOrder;

    public OrderItem(long id, Price price, Product product, int quantity, Order parentOrder) {
        this.id = id;
        this.price = price;
        this.product = product;
        this.quantity = quantity;
        this.parentOrder = parentOrder;
    }

    @SuppressWarnings("unused") // Needed for JPA
    public OrderItem() {
        this(0L, new Price(), new Product(), UNSET_QUANTITY, new Order());
    }

    public OrderItem(Price price, Product product, int quantity, Order parentOrder) {
        this(0L, price, product, quantity, parentOrder);
    }

    public Order getParentOrder() {
        return parentOrder;
    }

    BigDecimal getAmount() {
        return price.getAmount().multiply(new BigDecimal(quantity));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderItem orderItem = (OrderItem) o;
        return id == orderItem.id &&
                quantity == orderItem.quantity &&
                price.equals(orderItem.price) &&
                product.equals(orderItem.product);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public Price getPrice() {
        return price;
    }

    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    public Long getId() {
        return id;
    }

    @JsonIgnore
    public boolean isUnset() {
        return this.getQuantity() == OrderItem.UNSET_QUANTITY || getPrice().isUnset();
    }
}
