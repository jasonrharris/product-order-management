package com.jasonrharris.orders;

import com.jasonrharris.products.Price;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;

public class OrderTest {

    @Test
    public void getTotalAmount() {
        OrderItem orderItem = new OrderItem(1L, Price.createPrice(null, "20.50", "GBP"), null, 2, null);
        OrderItem orderItem2 = new OrderItem(2L, Price.createPrice(null, "30.50", "GBP"), null, 1, null);
        Order order = new Order(1L, LocalDateTime.now(), "buyer@gamil.com",
                new HashSet<>(Arrays.asList(orderItem, orderItem2)));

        Assert.assertEquals(order.getTotalAmount(),new BigDecimal("71.50"));
    }
}
