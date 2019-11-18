package com.jasonrharris.orders;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Entity(name="Order_")
public class Order {
    @Id
    @SequenceGenerator(name= "ORDER_SEQUENCE", sequenceName = "ORDER_SEQUENCE_ID", initialValue=1, allocationSize = 1)
    @GeneratedValue(strategy= GenerationType.AUTO, generator="ORDER_SEQUENCE")
    private final long id;

    @Column
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private final LocalDateTime creationDateTime;

    @Column
    private final String buyersEmail;

    @OneToMany(mappedBy = "parentOrder")
    private final Set<OrderItem> orderItems;

    @Transient
    private BigDecimal totalAmount;

    public Order(long id, LocalDateTime creationDateTime, String buyersEmail, Set<OrderItem> orderItems) {
        this.id = id;
        this.creationDateTime = creationDateTime;
        this.buyersEmail = buyersEmail;
        this.orderItems = orderItems;
        this.totalAmount = getTotalAmount(orderItems);
    }

    Order() {
        this(0L, LocalDateTime.now(), "", new HashSet<>());
    }

    @PostLoad
    @PostUpdate
    public void updateTotalAmount(){
        totalAmount = getTotalAmount(orderItems);
    }

    private BigDecimal getTotalAmount(@NotNull Set<OrderItem> orderItems) {
        return orderItems.stream().map(OrderItem::getAmount).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
    }

    public long getId() {
        return id;
    }

    public LocalDateTime getCreationDateTime() {
        return creationDateTime;
    }

    public String getBuyersEmail() {
        return buyersEmail;
    }

    public Set<OrderItem> getOrderItems() {
        return orderItems;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return id == order.id &&
                buyersEmail.equals(order.buyersEmail) &&
                orderItems.equals(order.orderItems);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
